package org.openmrs.module.gpconnect.services;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.BAD_REQUEST;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.DUPLICATE_REJECTED;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_NHS_NUMBER;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_RESOURCE;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.xpath.operations.Bool;
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.*;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.exceptions.GPConnectCoding;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.mappers.valueSets.RegistrationType;
import org.openmrs.module.gpconnect.util.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GPConnectPatientService {

    @Autowired
    FhirPatientService fhirPatientService;

    @Autowired
    FhirPatientDao fhirPatientDao;

    @Autowired
    NhsPatientMapper nhsPatientMapper;

    @Autowired
    NhsPatientService nhsPatientService;

    public org.openmrs.Patient save(Patient dstu3Patient, boolean isTemporaryPatient) {
        PatientRegistrationValidator.validate(dstu3Patient, isTemporaryPatient);

        String nhsNumber = dstu3Patient.getIdentifier().get(0).getValue();
        Collection<org.openmrs.Patient> patients = findByNhsNumber(nhsNumber);

        if (patients.size() > 0) {
            throw GPConnectExceptions.resourceVersionConflictException("Nhs Number already in use", DUPLICATE_REJECTED);
        }

        org.hl7.fhir.r4.model.Patient r4Patient
                = Patient30_40.convertPatient(dstu3Patient);
        fhirPatientService.create(r4Patient);

        org.openmrs.Patient newPatient = findByNhsNumber(nhsNumber).iterator().next();
        NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(dstu3Patient, newPatient.getPatientId());

        if(isTemporaryPatient) {
            setTempRegistrationDetails(nhsPatient);
        }

        nhsPatientService.saveOrUpdate(nhsPatient);

        return newPatient;
    }

    private void setTempRegistrationDetails(NhsPatient nhsPatient) {
        Date registrationStartDate = new Date();
        nhsPatient.setRegistrationType("T");
        nhsPatient.setRegistrationStart(registrationStartDate);
        nhsPatient.setRegistrationEnd(DateUtils.addMonths(registrationStartDate, 3));
    }

    private Collection<org.openmrs.Patient> findByNhsNumber(String nhsNumber) {
        TokenAndListParam identifier = new TokenAndListParam()
                .addAnd(new TokenParam(Extensions.NHS_NUMBER_SYSTEM, nhsNumber));

        SearchParameterMap params = (new SearchParameterMap()).addParameter("identifier.search.handler", identifier);

        List<String> resultUuids = fhirPatientDao.getSearchResultUuids(params);
        Collection<org.openmrs.Patient> patients = fhirPatientDao.getSearchResults(params, resultUuids);
        return patients;
    }
}

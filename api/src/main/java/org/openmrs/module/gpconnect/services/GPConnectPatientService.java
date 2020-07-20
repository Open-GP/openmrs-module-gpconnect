package org.openmrs.module.gpconnect.services;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.util.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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

    public org.openmrs.Patient save(Patient dstu3Patient) {
        if (dstu3Patient.getIdentifier().isEmpty()) {
            throw GPConnectExceptions.badRequest("Patient is missing id", "INVALID_NHS_NUMBER");
        }

        String nhsNumber = dstu3Patient.getIdentifier().get(0).getValue();

        if (nhsNumber.length() != 10) {
            throw GPConnectExceptions.badRequest("NHS Number is invalid", "INVALID_NHS_NUMBER");
        }

        if (dstu3Patient.getBirthDate() == null) {
            throw GPConnectExceptions.badRequest("Birth date is mandatory", "BAD_REQUEST");
        }

        if (!hasValidNames(dstu3Patient)) {
            throw GPConnectExceptions.badRequest("Patient must have an official name containing at least a family name", "BAD_REQUEST");
        }


        Collection<org.openmrs.Patient> patients = findByNhsNumber(nhsNumber);
        if (patients.size() > 0) {
            throw GPConnectExceptions.conflictException("Nhs Number already in use", "DUPLICATE_REJECTED");
        }

        org.hl7.fhir.r4.model.Patient r4Patient
                = Patient30_40.convertPatient(dstu3Patient);
        fhirPatientService.create(r4Patient);

        org.openmrs.Patient newPatient = findByNhsNumber(nhsNumber).iterator().next();
        NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(dstu3Patient, newPatient.getPatientId());

        nhsPatientService.saveOrUpdate(nhsPatient);

        return newPatient;
    }

    private Collection<org.openmrs.Patient> findByNhsNumber(String nhsNumber) {
        TokenAndListParam identifier = new TokenAndListParam()
                .addAnd(new TokenParam(Extensions.NHS_NUMBER_SYSTEM, nhsNumber));

        SearchParameterMap params = (new SearchParameterMap()).addParameter("identifier.search.handler", identifier);

        List<String> resultUuids = fhirPatientDao.getResultUuids(params);
        Collection<org.openmrs.Patient> patients = fhirPatientDao.search(params, resultUuids);
        return patients;
    }

    private boolean hasValidNames(Patient patient) {
        Optional<HumanName> officialName = patient.getName().stream().filter(humanName -> humanName.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst();
        return officialName.map(humanName -> (humanName.getFamily() != null) && (!humanName.getFamily().isEmpty())).orElse(false);
    }
}

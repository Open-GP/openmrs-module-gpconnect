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
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.*;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
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

    public org.openmrs.Patient save(Patient dstu3Patient) {
        if (dstu3Patient.getIdentifier().isEmpty()) {
            throw GPConnectExceptions.invalidRequestException("Patient is missing id", INVALID_NHS_NUMBER);
        }

        String nhsNumber = dstu3Patient.getIdentifier().get(0).getValue();

        if (nhsNumber.length() != 10) {
            throw GPConnectExceptions.invalidRequestException("NHS Number is invalid", INVALID_NHS_NUMBER);
        }

        dstu3Patient.getIdentifier().stream().forEach(identifier ->{
            if(!identifier.hasSystem())
                throw GPConnectExceptions.invalidRequestException("Identifier is missing System", BAD_REQUEST);
        });

        if (dstu3Patient.getBirthDate() == null) {
            throw GPConnectExceptions.invalidRequestException("Birth date is mandatory", BAD_REQUEST);
        }

        if (!hasValidNames(dstu3Patient)) {
            throw GPConnectExceptions.invalidRequestException("Patient must have an official name containing at least a family name", BAD_REQUEST);
        }

        if(dstu3Patient.hasAnimal()){
            throw GPConnectExceptions.unprocessableEntityException("Not allowed field: Animal", INVALID_RESOURCE);
        }

        if(dstu3Patient.hasCommunication()){
            throw GPConnectExceptions.unprocessableEntityException("Not allowed field: Communication", INVALID_RESOURCE);
        }

        if(dstu3Patient.hasPhoto()){
            throw GPConnectExceptions.unprocessableEntityException("Not allowed field: Photo", INVALID_RESOURCE);
        }

        validateTelecomUses(dstu3Patient);

        if(dstu3Patient.hasDeceasedBooleanType()){
            throw GPConnectExceptions.unprocessableEntityException("Not allowed field: Deceased", INVALID_RESOURCE);
        }

        Collection<org.openmrs.Patient> patients = findByNhsNumber(nhsNumber);
        if (patients.size() > 0) {
            throw GPConnectExceptions.resourceVersionConflictException("Nhs Number already in use", DUPLICATE_REJECTED);
        }

        org.hl7.fhir.r4.model.Patient r4Patient
                = Patient30_40.convertPatient(dstu3Patient);
        fhirPatientService.create(r4Patient);

        org.openmrs.Patient newPatient = findByNhsNumber(nhsNumber).iterator().next();
        NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(dstu3Patient, newPatient.getPatientId());

        setTempRegistrationDetails(nhsPatient);

        nhsPatientService.saveOrUpdate(nhsPatient);

        return newPatient;
    }

    private void setTempRegistrationDetails(NhsPatient nhsPatient) {
        Date registrationStartDate = new Date();
        nhsPatient.setRegistrationType("T");
        nhsPatient.setRegistrationStart(registrationStartDate);
        nhsPatient.setRegistrationEnd(DateUtils.addMonths(registrationStartDate, 3));
    }

    private void validateTelecomUses(Patient dstu3Patient) {
        List<ContactPoint> telecomList = dstu3Patient.getTelecom();
        Map<List<String>, Long> contactPointUseCount = telecomList.stream()
                .collect(Collectors.groupingBy( x -> Arrays.asList(x.getSystem().getDisplay(), x.getUse().getDisplay()),
                        Collectors.counting()));
        contactPointUseCount.values().removeIf( value -> value < 2);

        if(!contactPointUseCount.isEmpty()){
            StringBuilder errorMessage = new StringBuilder("Invalid telecom. Duplicate use of: ");

            contactPointUseCount.keySet().forEach( key -> {
                errorMessage.append("{System: ");
                errorMessage.append(key.get(0));
                errorMessage.append(", Use: ");
                errorMessage.append(key.get(1));
                errorMessage.append("}, ");
            });

            errorMessage.setLength(errorMessage.length() - 2);

            throw GPConnectExceptions.invalidRequestException(errorMessage.toString(), BAD_REQUEST);
        }
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

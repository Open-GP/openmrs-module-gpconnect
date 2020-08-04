package org.openmrs.module.gpconnect.services;

import org.hl7.fhir.dstu3.model.Address;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;

import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.*;

public class PatientRegistrationValidator {

    public static void validate(Patient dstu3Patient, boolean isTemporaryPatient){
        validateMandatoryFields(dstu3Patient);

        validateDisallowedFields(dstu3Patient, isTemporaryPatient);

        validateAddress(dstu3Patient);

        validateTelecomUses(dstu3Patient);
    }

    private static void validateDisallowedFields(Patient dstu3Patient, boolean isTemporaryPatient) {
        if(isTemporaryPatient) {
            ArrayList<FieldValidator> disallowedFields = new ArrayList<FieldValidator>(){{
                add(new FieldValidator(dstu3Patient.hasAnimal(), "Animal"));
                add(new FieldValidator(dstu3Patient.hasCommunication(), "Communication"));
                add(new FieldValidator(dstu3Patient.hasPhoto(), "Photo"));
                add(new FieldValidator(dstu3Patient.hasMultipleBirth(), "Multiple Births"));
                add(new FieldValidator(dstu3Patient.hasMaritalStatus(), "Marital Status"));
                add(new FieldValidator(dstu3Patient.hasDeceasedBooleanType(), "Deceased"));
                add(new FieldValidator(dstu3Patient.hasActive(), "Active"));
                add(new FieldValidator(dstu3Patient.hasContact(), "Contact"));
                add(new FieldValidator(dstu3Patient.hasGeneralPractitioner(), "General Practitioner"));
                add(new FieldValidator(dstu3Patient.hasManagingOrganization(), "Managing Organisation"));
            }};
            disallowedFields.forEach(FieldValidator::validateDisallowedField);
        }
    }

    private static void validateMandatoryFields(Patient dstu3Patient) {
        new FieldValidator(dstu3Patient.hasIdentifier(), "Patient is missing id", INVALID_NHS_NUMBER).validateMandatoryField();
        String nhsNumber = dstu3Patient.getIdentifier().get(0).getValue();

        ArrayList<FieldValidator> mandatoryFields = new ArrayList<FieldValidator>(){{
            add(new FieldValidator( hasValidNhsNumber(nhsNumber), "NHS Number is invalid", INVALID_NHS_NUMBER));
            add(new FieldValidator( dstu3Patient.hasBirthDate(), "Birth date is mandatory"));
            add(new FieldValidator( hasValidNames(dstu3Patient), "Patient must have an official name containing at least a family name"));
        }};
        dstu3Patient.getIdentifier().forEach(identifier ->{
            mandatoryFields.add(new FieldValidator( identifier.hasSystem(), "Identifier is missing System"));
        });
        mandatoryFields.forEach(FieldValidator::validateMandatoryField);
    }

    private static boolean hasValidNhsNumber(String nhsNumber) {
        return nhsNumber.length() == 10;
    }

    private static boolean hasValidNames(Patient patient) {
        Optional<HumanName> officialName = patient.getName().stream().filter(humanName -> humanName.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst();
        return officialName.map(humanName -> (humanName.getFamily() != null) && (!humanName.getFamily().isEmpty())).orElse(false);
    }

    private static void validateTelecomUses(Patient dstu3Patient) {
        List<ContactPoint> telecomList = dstu3Patient.getTelecom();
        Map<List<String>, Long> contactPointUseCount = telecomList.stream()
                .collect(Collectors.groupingBy(x -> Arrays.asList(x.getSystem().getDisplay(), x.getUse().getDisplay()),
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

    private static void validateAddress(Patient patient){
        List<Address.AddressUse> validUses  = Arrays.asList(Address.AddressUse.HOME, Address.AddressUse.TEMP);
        StringBuilder errorMessage = new StringBuilder("Invalid Address type: ");

        for (Address address : patient.getAddress()) {
            if (!validUses.contains(address.getUse())){
                errorMessage.append(address.getUse().name());
                throw GPConnectExceptions.unprocessableEntityException(errorMessage.toString(), INVALID_RESOURCE);
            }
        }
    }
}


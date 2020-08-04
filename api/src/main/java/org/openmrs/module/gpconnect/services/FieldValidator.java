package org.openmrs.module.gpconnect.services;

import org.openmrs.module.gpconnect.exceptions.GPConnectCoding;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.BAD_REQUEST;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_RESOURCE;

class FieldValidator{
    private final boolean hasField;
    private final String errorMessage;
    private GPConnectCoding mandatoryFieldCoding = BAD_REQUEST;

    public FieldValidator(boolean hasField, String errorMessage){
        this.hasField = hasField;
        this.errorMessage = errorMessage;
    }

    public FieldValidator(boolean hasField, String errorMessage, GPConnectCoding gpConnectCoding){
        this.hasField = hasField;
        this.errorMessage = errorMessage;
        this.mandatoryFieldCoding = gpConnectCoding;
    }

    public void validateMandatoryField(){
        if(!hasField){
            throw GPConnectExceptions.invalidRequestException(errorMessage, mandatoryFieldCoding);
        }
    }

    public void validateDisallowedField(){
        if(hasField){
            String fieldErrorMessage = String.format("Not allowed field: %s", errorMessage);
            throw GPConnectExceptions.unprocessableEntityException(fieldErrorMessage, INVALID_RESOURCE);
        }
    }
}

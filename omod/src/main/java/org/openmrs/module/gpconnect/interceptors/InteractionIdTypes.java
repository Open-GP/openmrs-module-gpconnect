package org.openmrs.module.gpconnect.interceptors;

public enum InteractionIdTypes {

    PRACTITIONER_READ_ID("urn:nhs:names:services:gpconnect:fhir:rest:read:practitioner-1"),
    LOCATION_READ_ID("urn:nhs:names:services:gpconnect:fhir:rest:read:location-1");

    private final String id;

    InteractionIdTypes(String id) {
        this.id = id;
    }

    public String getId(){
        return id;
    }
}


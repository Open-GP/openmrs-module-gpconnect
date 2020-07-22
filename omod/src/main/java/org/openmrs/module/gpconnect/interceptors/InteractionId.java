package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;

public class InteractionId {
	
	private final String resource;
	
	private final String action;
	
	private final RestOperationTypeEnum operationType;
	
	public InteractionId(String resource, String action, RestOperationTypeEnum operationType) {
		this.resource = resource;
		this.action = action;
		this.operationType = operationType;
	}
	
	public String getResource() {
		return resource;
	}
	
	public String getActionName() {
		return operationType.name();
	}
	
	public boolean hasMatchingRequest(RequestDetails requestDetails) {
		return requestDetails.getResourceName() != null && requestDetails.getResourceName().equals(resource);
	}
	
	public boolean hasMatchingAction(RestOperationTypeEnum requestOperationType) {
		return requestOperationType.name().equals(operationType.name());
	}
	
	public boolean hasMatchingInteractionId(RequestDetails requestDetails) {
		String interactionId = requestDetails.getHeader("Ssp-InteractionID");
		return interactionId == null || !interactionId.equals(getIdType());
	}
	
	private String getIdType() {
		return String.format("urn:nhs:names:services:gpconnect:fhir:rest:%s:%s-1", action.toLowerCase(),
		    resource.toLowerCase());
	}
}

package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;

import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

import java.util.ArrayList;
import java.util.List;


import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;


@Interceptor
public class InteractionIdInterceptor {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleInteractionId(RequestDetails requestDetails, ServletRequestDetails servletRequestDetails, RestOperationTypeEnum operationType) {
        List<InteractionId> interactions = new ArrayList<InteractionId>();
        interactions.add(new InteractionId("Practitioner", "READ", RestOperationTypeEnum.READ));
        interactions.add(new InteractionId("Practitioner", "SEARCH",  RestOperationTypeEnum.SEARCH_TYPE));
        interactions.add(new InteractionId("Location", "READ",  RestOperationTypeEnum.READ));

        interactions.forEach(interaction -> {
            if (interaction.hasMatchingRequest(requestDetails) && 
                interaction.hasMatchingAction(operationType) &&
                interaction.hasMatchingInteractionId(requestDetails)) {
                throw createBadRequest(String.format("Interaction id does not match resource: %s, action: %s", interaction.getResource(), interaction.getActionName()));     
            }
        });
    }

    private InvalidRequestException createBadRequest(String errorMessage) {
        OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
        return new InvalidRequestException("BAD REQUEST", operationOutcome);
    }
}

class InteractionId{

    private final String resource;
    private final String action;
    private final RestOperationTypeEnum operationType;

    public InteractionId(String resource, String action, RestOperationTypeEnum operationType){
        this.resource = resource;
        this.action = action;
        this.operationType = operationType;
    }

    public String getResource(){
        return resource;
    }

    public String getActionName(){
        return operationType.name();
    }

    public boolean hasMatchingRequest(RequestDetails requestDetails){
        return requestDetails.getResourceName() != null && requestDetails.getResourceName().equals(resource);
    }

    public boolean hasMatchingAction(RestOperationTypeEnum requestOperationType){
        return requestOperationType.name().equals(operationType.name());
    }

    public boolean hasMatchingInteractionId(RequestDetails requestDetails){
        String interactionId = requestDetails.getHeader("Ssp-InteractionID");
        return interactionId == null || !interactionId.equals(getIdType());
    }

    private String getIdType(){
        return String.format("urn:nhs:names:services:gpconnect:fhir:rest:%s:%s-1", action.toLowerCase(), resource.toLowerCase());
    }
}

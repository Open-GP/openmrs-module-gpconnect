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
        List<InteractionId> interactions = new ArrayList<>();
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
        OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "BAD_REQUEST", "Bad request", OperationOutcome.IssueType.INVALID);
        return new InvalidRequestException("BAD REQUEST", operationOutcome);
    }
}



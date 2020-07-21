package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;

@Interceptor
public class InteractionIdInterceptor {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleInteractionId(RequestDetails requestDetails, RestOperationTypeEnum operationType) {
        handleResourceOrActionNotMatchingInteractionId(requestDetails, operationType, "Practitioner", InteractionIdTypes.PRACTITIONER_READ_ID.getId(), RestOperationTypeEnum.READ);
        handleResourceOrActionNotMatchingInteractionId(requestDetails, operationType, "Practitioner", InteractionIdTypes.PRACTITIONER_SEARCH_ID.getId(), RestOperationTypeEnum.SEARCH_TYPE);
        handleResourceOrActionNotMatchingInteractionId(requestDetails, operationType, "Location", InteractionIdTypes.LOCATION_READ_ID.getId(), RestOperationTypeEnum.READ);

    }

    private InvalidRequestException createBadRequest(String errorMessage) {
        OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
        return new InvalidRequestException("BAD REQUEST", operationOutcome);
    }

    private boolean doesRequestMatchResourceAndAction(RequestDetails requestDetails, String resourceName, RestOperationTypeEnum action, RestOperationTypeEnum operationType) {
        return requestDetails.getResourceName() != null && requestDetails.getResourceName().equals(resourceName) && operationType.equals(action);
    }

    private void handleResourceOrActionNotMatchingInteractionId(
            RequestDetails requestDetails,
            RestOperationTypeEnum operationType,
            String expectedResourceName,
            String expectedInteractionId,
            RestOperationTypeEnum expectedResourceAction) {
        String interactionId = requestDetails.getHeader("Ssp-InteractionID");
        if (doesRequestMatchResourceAndAction(requestDetails, expectedResourceName, expectedResourceAction, operationType)) {
            if (interactionId == null || !interactionId.equals(expectedInteractionId)) {
                throw createBadRequest(String.format("Interaction id does not match resource: %s, action: %s", expectedResourceName, expectedResourceAction));
            }
        }
    }
}

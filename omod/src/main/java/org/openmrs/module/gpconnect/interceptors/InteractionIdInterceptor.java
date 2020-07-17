package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.utilities.graphql.Operation;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;

@Interceptor
public class InteractionIdInterceptor {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleInteractionId(RequestDetails requestDetails, ServletRequestDetails servletRequestDetails, RestOperationTypeEnum operationType) {
        handleResourceNotMatchingInteractionIdForReadAction(requestDetails, operationType, "Practitioner", InteractionIdTypes.PRACTITIONER_READ_ID);
        handleResourceNotMatchingInteractionIdForReadAction(requestDetails, operationType, "Location", InteractionIdTypes.LOCATION_READ_ID);

    }

    private InvalidRequestException createBadRequest(String errorMessage) {
        OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
        return new InvalidRequestException("BAD REQUEST", operationOutcome);
    }

    private boolean checksRequestMatchesSelectedResourceAndIsReadAction(RequestDetails requestDetails, RestOperationTypeEnum operationType, String resourceName) {
        return requestDetails.getResourceName() != null && requestDetails.getResourceName().equals(resourceName) && operationType.name().equals("READ");
    }

    private void handleResourceNotMatchingInteractionIdForReadAction(
            RequestDetails requestDetails,
            RestOperationTypeEnum operationType,
            String resourceName,
            InteractionIdTypes interactionIdType) {
        String interactionId = requestDetails.getHeader("Ssp-InteractionID");
        if (checksRequestMatchesSelectedResourceAndIsReadAction(requestDetails, operationType, resourceName)) {
            if (interactionId == null || !interactionId.equals(interactionIdType.getId())) {
                throw createBadRequest(String.format("Interaction id does not match resource: %s, action: READ", resourceName));
            }
        }
    }
}

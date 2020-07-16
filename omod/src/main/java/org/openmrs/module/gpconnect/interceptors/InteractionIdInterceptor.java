package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;

@Interceptor
public class InteractionIdInterceptor {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleInteractionId(RequestDetails requestDetails, ServletRequestDetails servletRequestDetails, RestOperationTypeEnum operationType) {
        String interactionId = requestDetails.getHeader("Ssp-InteractionID");
        if (isResourcePractitionerAndActionRead(requestDetails, operationType)) {
            if (interactionId == null || !interactionId.equals(InteractionIdTypes.PRACTITIONER_READ_ID.getId())) {
                throw createBadRequest("Interaction id does not match resource: Practitioner, action: READ");
            }  
        } 
    }

    private InvalidRequestException createBadRequest(String errorMessage) {
        OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
        return new InvalidRequestException("BAD REQUEST", operationOutcome);
    }

    private boolean isResourcePractitionerAndActionRead(RequestDetails requestDetails, RestOperationTypeEnum operationType) {
        return requestDetails.getResourceName() != null && requestDetails.getResourceName().equals("Practitioner") && operationType.name().equals("READ");
    }
}

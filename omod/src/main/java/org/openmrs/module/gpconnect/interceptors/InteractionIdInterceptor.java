package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.util.CodeSystems;

@Interceptor
public class InteractionIdInterceptor {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleInteractionId(RequestDetails requestDetails, ServletRequestDetails servletRequestDetails, RestOperationTypeEnum operationType) {
        String interactionId = requestDetails.getHeader("Ssp-InteractionID");
        if (interactionId == null && requestDetails.getResourceName().equals("Practitioner") && operationType.name().equals("READ")) {
            Coding coding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "BAD_REQUEST", "BAD_REQUEST");
            OperationOutcome operationOutcome = OperationOutcomeCreator.createErrorOperationOutcome("No interaction id present in the request", coding, OperationOutcome.IssueType.INVALID);
            throw new InvalidRequestException("BAD REQUEST", operationOutcome);
        }
    }
}

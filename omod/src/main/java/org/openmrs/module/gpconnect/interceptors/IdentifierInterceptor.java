package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;

@Interceptor
public class IdentifierInterceptor {
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleIdentifier(RequestDetails requestDetails, ServletRequestDetails servletRequestDetails, RestOperationTypeEnum operationType) {
        if (requestDetails.getResourceName().equals("Practitioner") && operationType.name().equals("SEARCH_TYPE")) {
            if (areThereMultipleValuesSeparatedByACommaForIdentifier(requestDetails)) {
                String errorMessage = "Multiple values detected for non-repeatable parameter 'identifier'."
                    + "This server is not configured to allow multiple (AND/OR) values for this param.";
                OperationOutcome operationOutcome = OperationOutcomeCreator
                    .build(errorMessage, "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
                throw new InvalidRequestException("BAD REQUEST", operationOutcome);
            } else if (areThereMultipleValuesSeparatedByAPipeForIdentifier(requestDetails)) {
                String identifierSystemAndValue = requestDetails.getParameters().get("identifier")[0];
                String errorMessage = "One or both of the identifier system and value are missing from given identifier : " + identifierSystemAndValue;
                OperationOutcome operationOutcome = OperationOutcomeCreator
                    .build(errorMessage, "INVALID_PARAMETER", OperationOutcome.IssueType.INVALID);
                throw new UnprocessableEntityException("INVALID_PARAMETER", operationOutcome);
            }
        }
    }

    private boolean areThereMultipleValuesSeparatedByAPipeForIdentifier(RequestDetails requestDetails) {
        return requestDetails.getParameters().get("identifier")[0].split("\\|").length > 2;
    }

    private boolean areThereMultipleValuesSeparatedByACommaForIdentifier(RequestDetails requestDetails) {
        return requestDetails.getParameters().get("identifier")[0].split("\\|")[1].split(",").length > 1;
    }
}

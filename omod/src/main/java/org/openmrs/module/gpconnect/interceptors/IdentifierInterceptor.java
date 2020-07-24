package org.openmrs.module.gpconnect.interceptors;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_IDENTIFIER_VALUE;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;

@Interceptor
public class IdentifierInterceptor {
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handleIdentifier(RequestDetails requestDetails, RestOperationTypeEnum operationType) {
        if (requestDetails.getResourceName() != null && requestDetails.getResourceName().equals("Practitioner") && operationType.name().equals("SEARCH_TYPE") && requestDetails.getParameters().size() >= 1) {
            if (isTheIdentifierSystemAndValueNotEmpty(requestDetails) && areThereMultipleValuesSeparatedByACommaForIdentifier(requestDetails)) {
                throw GPConnectExceptions.invalidRequestException("Multiple values detected for non-repeatable parameter 'identifier'."
                    + "This server is not configured to allow multiple (AND/OR) values for this param.", INVALID_IDENTIFIER_VALUE);
            } else if (isTheIdentifierSystemEmpty(requestDetails) || isTheIdentifierValueNotGiven(requestDetails)) {
                String identifierSystemAndValue = requestDetails.getParameters().get("identifier")[0];
                String errorMessage = "One or both of the identifier system and value are missing from given identifier : " + identifierSystemAndValue;
                OperationOutcome operationOutcome = OperationOutcomeCreator
                    .build(errorMessage, "INVALID_PARAMETER", "Submitted parameter is not valid.", OperationOutcome.IssueType.INVALID);
                throw new UnprocessableEntityException("INVALID_PARAMETER", operationOutcome);
            } else if (areThereMultipleValuesSeparatedByAPipeForIdentifier(requestDetails)) {
                String identifierSystemAndValue = requestDetails.getParameters().get("identifier")[0];
                throw GPConnectExceptions.unprocessableEntityException(
                    "One or both of the identifier system and value are missing from given identifier : " + identifierSystemAndValue, INVALID_IDENTIFIER_VALUE);
            }
        }
    }

    private boolean isTheIdentifierValueNotGiven(RequestDetails requestDetails) {
        return !requestDetails.getParameters().get("identifier")[0].contains("|") && requestDetails.getParameters().get("identifier")[0].split("\\|").length == 1;
    }

    private boolean isTheIdentifierSystemAndValueNotEmpty(RequestDetails requestDetails) {
        return requestDetails.getParameters().get("identifier")[0].split("\\|").length == 2;
    }

    private boolean isTheIdentifierSystemEmpty(RequestDetails requestDetails) {
        return requestDetails.getParameters().get("identifier")[0].isEmpty();
    }

    private boolean areThereMultipleValuesSeparatedByAPipeForIdentifier(RequestDetails requestDetails) {
        return requestDetails.getParameters().get("identifier")[0].split("\\|").length > 2;
    }

    private boolean areThereMultipleValuesSeparatedByACommaForIdentifier(RequestDetails requestDetails) {
        return requestDetails.getParameters().get("identifier")[0].split("\\|")[1].split(",").length > 1;
    }
}

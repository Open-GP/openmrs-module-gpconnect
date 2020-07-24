package org.openmrs.module.gpconnect.interceptors;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_IDENTIFIER_VALUE;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_PARAMETER;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;

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
                throw GPConnectExceptions.unprocessableEntityException(
                    "One or both of the identifier system and value are missing from given identifier : " + identifierSystemAndValue, INVALID_PARAMETER);
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

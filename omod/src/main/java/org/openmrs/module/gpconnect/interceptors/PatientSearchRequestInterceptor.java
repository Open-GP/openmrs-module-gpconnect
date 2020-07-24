package org.openmrs.module.gpconnect.interceptors;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.BAD_REQUEST;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;

@Interceptor
public class PatientSearchRequestInterceptor {

    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handlePatientSearchRequest(RequestDetails requestDetails, RestOperationTypeEnum operationType) {
        String resourceName = requestDetails.getResourceName();
        if (resourceName != null && resourceName.equals("Patient") && operationType == RestOperationTypeEnum.SEARCH_TYPE && requestDetails.getParameters().isEmpty()) {
            throw GPConnectExceptions.invalidRequestException("Searching without any parameters is not possible", BAD_REQUEST);
        }
    }
}

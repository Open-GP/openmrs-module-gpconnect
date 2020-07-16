package org.openmrs.module.gpconnect.interceptors;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;
import org.openmrs.module.gpconnect.util.CodeSystems;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Interceptor;
import ca.uhn.fhir.interceptor.api.Pointcut;
import ca.uhn.fhir.rest.api.RestOperationTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.servlet.ServletRequestDetails;

@Interceptor
public class PatientSearchRequestInterceptor {
    
    @Hook(Pointcut.SERVER_INCOMING_REQUEST_PRE_HANDLED)
    public void handlePatientSearchRequest(RequestDetails requestDetails, ServletRequestDetails servletRequestDetails, RestOperationTypeEnum operationType){
        String resourceName = requestDetails.getResourceName();
        if(resourceName != null && resourceName.equals("Patient") && operationType == RestOperationTypeEnum.SEARCH_TYPE && requestDetails.getParameters().isEmpty()){
            OperationOutcome operationOutcome = OperationOutcomeCreator.build("Searching without any parameters is not possible", "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
            throw new InvalidRequestException("BAD REQUEST", operationOutcome);
        }
    }
}

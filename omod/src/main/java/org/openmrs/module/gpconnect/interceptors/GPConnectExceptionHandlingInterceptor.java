package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;

public class GPConnectExceptionHandlingInterceptor extends ExceptionHandlingInterceptor {
	
	@Override
	public BaseServerResponseException preProcessOutgoingException(RequestDetails theRequestDetails, Throwable theException, HttpServletRequest theServletRequest) throws ServletException {
		if (theException instanceof InvalidRequestException
		        && theRequestDetails.getCompleteUrl().endsWith("$gpc.registerpatient")
		        && theRequestDetails.getResource() == null) {
			return new UnprocessableEntityException("Unknown resource type");
		}

		if(theException instanceof InvalidRequestException
			&&  theRequestDetails.getParameters().size() > 0
			&&  theRequestDetails.getParameters().get("identifier") == null
			&&  theRequestDetails.getRequestType() == RequestTypeEnum.GET) {
				String errorMessage = "Invalid parameter in request";
				OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "BAD_REQUEST", OperationOutcome.IssueType.INVALID);
				return new InvalidRequestException("BAD REQUEST", operationOutcome);
			}

		return super.preProcessOutgoingException(theRequestDetails, theException, theServletRequest);
	}
}

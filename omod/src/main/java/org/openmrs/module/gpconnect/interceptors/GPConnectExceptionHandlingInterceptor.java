package org.openmrs.module.gpconnect.interceptors;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.BAD_REQUEST;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;

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
				return GPConnectExceptions.invalidRequestException("Invalid parameter in request", BAD_REQUEST);
			}

		if(theException instanceof InvalidRequestException
				&&  theRequestDetails.getResourceName().equals("Patient")
				&&  !theRequestDetails.getCompleteUrl().endsWith("$gpc.registerpatient")
				&&  theRequestDetails.getRequestType() == RequestTypeEnum.POST) {
			String errorMessage = String.format("The following endpoint is invalid: %s", theRequestDetails.getRequestPath());
			return GPConnectExceptions.invalidRequestException(errorMessage, BAD_REQUEST);
		}


		return super.preProcessOutgoingException(theRequestDetails, theException, theServletRequest);
	}
}

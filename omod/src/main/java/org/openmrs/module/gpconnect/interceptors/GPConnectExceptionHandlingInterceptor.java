package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

public class GPConnectExceptionHandlingInterceptor extends ExceptionHandlingInterceptor {
	
	@Override
	public BaseServerResponseException preProcessOutgoingException(RequestDetails theRequestDetails, Throwable theException,
	        HttpServletRequest theServletRequest) throws ServletException {
		if (theException instanceof InvalidRequestException
		        && theRequestDetails.getCompleteUrl().endsWith("$gpc.registerpatient")
		        && theRequestDetails.getResource() == null) {
			return new UnprocessableEntityException("Unknown resource type");
		}
		return super.preProcessOutgoingException(theRequestDetails, theException, theServletRequest);
	}
}

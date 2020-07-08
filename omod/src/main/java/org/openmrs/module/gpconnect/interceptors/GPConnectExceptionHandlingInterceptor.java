package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;

public class GPConnectExceptionHandlingInterceptor extends ExceptionHandlingInterceptor {
	
	@Override
	public BaseServerResponseException preProcessOutgoingException(RequestDetails theRequestDetails, Throwable theException,
	        HttpServletRequest theServletRequest) throws ServletException {
		if (theException instanceof InvalidRequestException
		        && theRequestDetails.getCompleteUrl().endsWith("$gpc.registerpatient")
		        && theRequestDetails.getResource() == null) {
			return new UnprocessableEntityException("Unknown resource type");
		}

		if(theException instanceof InvalidRequestException 
			&&  theRequestDetails.getParameters().size() > 0
			&&  theRequestDetails.getRequestType() == RequestTypeEnum.GET){
				OperationOutcome op = new OperationOutcome();
				Meta m = new Meta();
				m.addProfile("http://fhir.nhs.net/StructureDefinition/gpconnect-operationoutcome-1");
				op.setMeta(m);
				return new InvalidRequestException("Invalid paramiter", op);
			}

		return super.preProcessOutgoingException(theRequestDetails, theException, theServletRequest);
	}
}

package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import ca.uhn.fhir.rest.server.interceptor.ExceptionHandlingInterceptor;

import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.UriType;
import org.openmrs.module.gpconnect.util.CodeSystems;

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
				String errorMessage = "Invalid paramiter in request";
				Coding coding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "BAD_REQUEST", "BAD_REQUEST");
				OperationOutcome operationOutcome = createErrorOperationOutcome(errorMessage, coding, OperationOutcome.IssueType.INVALID);
				return new InvalidRequestException("BAD REQUEST", operationOutcome);
			}

		return super.preProcessOutgoingException(theRequestDetails, theException, theServletRequest);
	}

	private OperationOutcome createErrorOperationOutcome(String errorMessage, Coding coding,
	OperationOutcome.IssueType issueType) {
		OperationOutcome operationOutcome = new OperationOutcome();
		
		Meta meta = new Meta();
		meta.setProfile(Collections.singletonList(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1")));
		operationOutcome.setMeta(meta);
		
		OperationOutcome.OperationOutcomeIssueComponent issue = new OperationOutcome.OperationOutcomeIssueComponent();
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setCode(issueType);
		CodeableConcept details = new CodeableConcept().setCoding(Collections.singletonList(coding));
		issue.setDetails(details);
		issue.setDiagnostics(errorMessage);
		operationOutcome.setIssue(Collections.singletonList(issue));
		return operationOutcome;
	}
}

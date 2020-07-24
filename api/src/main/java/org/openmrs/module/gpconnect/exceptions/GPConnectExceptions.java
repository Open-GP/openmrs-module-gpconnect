package org.openmrs.module.gpconnect.exceptions;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.OperationOutcome;

public class GPConnectExceptions {
	
	public static InvalidRequestException invalidRequestException(String errorMessage, GPConnectCoding gpConnectCoding) {
		OperationOutcome operationOutcome = getOperationOutcome(errorMessage, gpConnectCoding);
		return new InvalidRequestException(errorMessage, operationOutcome);
	}
	
	public static ResourceVersionConflictException resourceVersionConflictException(String errorMessage,
	        GPConnectCoding gpConnectCoding) {
		OperationOutcome operationOutcome = getOperationOutcome(errorMessage, gpConnectCoding);
		return new ResourceVersionConflictException(errorMessage, operationOutcome);
	}
	
	public static UnprocessableEntityException unprocessableEntityException(String errorMessage,
	        GPConnectCoding gpConnectCoding) {
		OperationOutcome operationOutcome = getOperationOutcome(errorMessage, gpConnectCoding);
		return new UnprocessableEntityException(errorMessage, operationOutcome);
	}
	
	public static ResourceNotFoundException resourceNotFoundException(String errorMessage, GPConnectCoding gpConnectCoding) {
		OperationOutcome operationOutcome = getOperationOutcome(errorMessage, gpConnectCoding);
		return new ResourceNotFoundException(errorMessage, operationOutcome);
	}
	
	private static OperationOutcome getOperationOutcome(String errorMessage, GPConnectCoding gpConnectCoding) {
		return OperationOutcomeCreator.build(errorMessage, gpConnectCoding.getCode(), gpConnectCoding.getDisplay(),
		    gpConnectCoding.getIssueType());
	}
}

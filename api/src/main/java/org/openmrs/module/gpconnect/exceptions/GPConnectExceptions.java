package org.openmrs.module.gpconnect.exceptions;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import org.hl7.fhir.dstu3.model.OperationOutcome;

public class GPConnectExceptions {
	
	public static InvalidRequestException badRequest(String errorMessage, String errorCode) {
		OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, errorCode,
		    OperationOutcome.IssueType.INVALID);
		return new InvalidRequestException(errorMessage, operationOutcome);
	}
	
	public static ResourceVersionConflictException conflictException(String errorMessage, String codingCode) {
		OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, codingCode,
		    OperationOutcome.IssueType.INVALID);
		
		throw new ResourceVersionConflictException(errorMessage, operationOutcome);
	}
}

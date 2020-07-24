package org.openmrs.module.gpconnect.exceptions;

import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.openmrs.module.gpconnect.util.CodeSystems;

public class OperationOutcomeCreator {
	
	public static OperationOutcome build(String errorMessage, String theCodingCode, String theCodingDisplay,
	        OperationOutcome.IssueType issueType) {
		Coding coding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, theCodingCode, theCodingDisplay);
		return new OperationOutcomeBuilder().setErrorMessage(errorMessage).setCoding(coding).setIssueType(issueType).build();
	}
}

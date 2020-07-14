package org.openmrs.module.gpconnect.exceptions;

import java.util.Collections;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.UriType;

public class OperationOutcomeBuilder {
	
	private String errorMessage;
	
	private Coding coding;
	
	private OperationOutcome.IssueType issueType;
	
	public OperationOutcomeBuilder() {
	}
	
	public OperationOutcomeBuilder setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
		return this;
	}
	
	public OperationOutcomeBuilder setCoding(Coding coding) {
		this.coding = coding;
		return this;
	}
	
	public OperationOutcomeBuilder setIssueType(OperationOutcome.IssueType issueType) {
		this.issueType = issueType;
		return this;
	}
	
	public OperationOutcome build() {
		OperationOutcome operationOutcome = new OperationOutcome();
		Meta meta = new Meta();
		meta.setProfile(Collections.singletonList(new UriType(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1")));
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

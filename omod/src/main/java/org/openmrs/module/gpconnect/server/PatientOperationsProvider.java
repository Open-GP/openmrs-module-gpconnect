package org.openmrs.module.gpconnect.server;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir.resources.FHIRPatientResource;
import org.openmrs.module.gpconnect.util.CodeSystems;

import java.util.Collections;

public class PatientOperationsProvider implements ca.uhn.fhir.rest.server.IResourceProvider {
	
	private FHIRPatientResource patientResource;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}
	
	public PatientOperationsProvider() {
		patientResource = new FHIRPatientResource();
	}
	
	@Operation(name = "$gpc.registerpatient")
	public Bundle registerPatient(@OperationParam(name = "registerPatient", type = Patient.class) Patient patient) {
		
		if (patient.getBirthDate() == null) {
			String errorMessage = "Birth date is missing";
			Coding invalidIdentifierCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "BAD_REQUEST",
			        "BAD_REQUEST");
			OperationOutcome invalidIdentifier = createErrorOperationOutcome(errorMessage, invalidIdentifierCoding,
			    OperationOutcome.IssueType.INVALID);
			throw new InvalidRequestException(errorMessage, invalidIdentifier);
		}
		
		Patient fhirPatient = patientResource.createFHIRPatient(patient);
		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
		entryComponent.setResource(fhirPatient);
		bundle.addEntry(entryComponent);
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setMeta(new Meta().setProfile(Collections.singletonList(new UriType(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-Searchset-Bundle-1"))));
		return bundle;
	}
	
	private OperationOutcome createErrorOperationOutcome(String errorMessage, Coding coding,
	        OperationOutcome.IssueType issueType) {
		OperationOutcome patientNotFound = new OperationOutcome();
		Meta meta = new Meta();
		meta.setProfile(Collections.singletonList(new UriType(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1")));
		
		patientNotFound.setMeta(meta);
		
		OperationOutcome.OperationOutcomeIssueComponent issue = new OperationOutcome.OperationOutcomeIssueComponent();
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setCode(issueType);
		CodeableConcept details = new CodeableConcept().setCoding(Collections.singletonList(coding));
		issue.setDetails(details);
		issue.setDiagnostics(errorMessage);
		patientNotFound.setIssue(Collections.singletonList(issue));
		return patientNotFound;
	}
	
}

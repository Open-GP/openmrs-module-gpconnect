package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.UriType;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.providers.r3.PatientFhirResourceProvider;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collections;

@Component
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
@Primary
public class GPConnectPatientProvider extends PatientFhirResourceProvider {
	
	@Autowired
	private FhirPatientService patientService;
	
	@Autowired
	NhsPatientMapper nhsPatientMapper;
	
	@Override
	@Read
	public Patient getPatientById(@IdParam @NotNull IdType id) {
		try {
			Patient patient = super.getPatientById(id);
			if (patient.getId() == null) {
				throw new ResourceNotFoundException(id);
			}
			
			return nhsPatientMapper.enhance(patient);
		}
		catch (Exception e) {
			System.out.println("catching exception");
			String errorMessage = "No patient details found for patient ID: Patient/" + id.getIdPart();
			Coding notFoundCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "PATIENT_NOT_FOUND",
			        "PATIENT_NOT_FOUND");
			OperationOutcome patientNotFound = createErrorOperationOutcome(errorMessage, notFoundCoding,
			    OperationOutcome.IssueType.INVALID);
			throw new ResourceNotFoundException(errorMessage, patientNotFound);
		}
	}
	
	@Operation(name = "$gpc.registerpatient")
	public MethodOutcome registerPatient(@OperationParam(name = "registerPatient", type = Patient.class) Patient patient) {
		
		try {
			org.hl7.fhir.r4.model.Patient receivedPatient = Patient30_40.convertPatient(patient);
			patientService.create(receivedPatient);
			return new MethodOutcome();
		}
		catch (Exception exception) {
			exception.printStackTrace();
			throw exception;
		}
		
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

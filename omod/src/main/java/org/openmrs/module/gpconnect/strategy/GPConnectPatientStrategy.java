package org.openmrs.module.gpconnect.strategy;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.UriType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir.api.strategies.patient.PatientStrategy;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component("GPConnectPatientStrategy")
public class GPConnectPatientStrategy extends PatientStrategy {
	
	@Autowired
	NhsPatientMapper nhsPatientMapper;
	
	@Autowired
	NhsPatientService nhsPatientService;
	
	@Override
	public Patient getPatient(String uuid) {
		Patient patient = super.getPatient(uuid);
		
		if (patient == null) {
			String errorMessage = "No patient details found for patient ID: Patient/" + uuid;
			Coding notFoundCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "PATIENT_NOT_FOUND",
			        "PATIENT_NOT_FOUND");
			OperationOutcome patientNotFound = createErrorOperationOutcome(errorMessage, notFoundCoding,
			    OperationOutcome.IssueType.INVALID);
			throw new ResourceNotFoundException(errorMessage, patientNotFound);
		}
		
		return nhsPatientMapper.enhance(patient);
	}
	
	@Override
	public List<Patient> searchPatientsById(String uuid) {
		List<Patient> patients = super.searchPatientsById(uuid);

		return patients.stream()
				.map(patient -> nhsPatientMapper.enhance(patient))
				.collect(Collectors.toList());
	}
	
	@Override
	public List<Patient> searchPatientsByIdentifier(String identifierValue, String identifierTypeName) {
		PatientService patientService = Context.getPatientService();

		if (identifierValue.isEmpty()) {
			throw createMissingIdentifierPartException(identifierTypeName + "|");
		}

		if (patientService.getPatientIdentifierTypeByName(identifierTypeName) == null){
			String errorMessage = String.format("The given identifier system code (%s) is not an expected code", identifierTypeName);
			Coding invalidIdentifierCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "INVALID_IDENTIFIER_SYSTEM", "INVALID_IDENTIFIER_SYSTEM");
			OperationOutcome invalidIdentifier = createErrorOperationOutcome(errorMessage, invalidIdentifierCoding, OperationOutcome.IssueType.INVALID);
			throw new InvalidRequestException(errorMessage, invalidIdentifier);
		}
		return super.searchPatientsByIdentifier(identifierValue, identifierTypeName).stream()
				.map(patient -> nhsPatientMapper.enhance(patient))
				.collect(Collectors.toList());
	}
	
	@Override
	public List<Patient> searchPatientsByIdentifier(String identifier) {
		throw createMissingIdentifierPartException(identifier);
	}
	
	@Override
	public List<Patient> searchPatients(boolean active) {
		return super.searchPatients(active)
				.stream()
				.map(patient -> nhsPatientMapper.enhance(patient))
				.collect(Collectors.toList());
	}
	
	@Override
	public Bundle searchPatientsByGivenName(String givenName) {
		Bundle bundle = super.searchPatientsByGivenName(givenName);

		bundle.getEntry()
				.forEach(bundleEntryComponent -> {
					Patient patient = (Patient) bundleEntryComponent.getResource();
					bundleEntryComponent.setResource(nhsPatientMapper.enhance(patient));
				});

		return bundle;

	}
	
	@Override
	public Bundle searchPatientsByFamilyName(String familyName) {
		Bundle bundle = super.searchPatientsByFamilyName(familyName);

		bundle.getEntry()
				.forEach(bundleEntryComponent -> {
					Patient patient = (Patient) bundleEntryComponent.getResource();
					bundleEntryComponent.setResource(nhsPatientMapper.enhance(patient));
				});

		return bundle;
	}
	
	@Override
	public Bundle searchPatientsByName(String name) {
		Bundle bundle = super.searchPatientsByName(name);

		bundle.getEntry()
				.forEach(bundleEntryComponent -> {
					Patient patient = (Patient) bundleEntryComponent.getResource();
					bundleEntryComponent.setResource(nhsPatientMapper.enhance(patient));
				});

		return bundle;
	}
	
	@Override
	public Bundle getPatientOperationsById(String patientId) {
		return super.getPatientOperationsById(patientId);
	}
	
	@Override
	public void deletePatient(String uuid) {
		super.deletePatient(uuid);
	}
	
	//Todo handle the case where writing the nhs patient fails - maybe reverting the write for the patient
	@Override
	public Patient createFHIRPatient(Patient patient) {
		Patient fhirPatient = super.createFHIRPatient(patient);
		
		org.openmrs.Patient patientByUuid = Context.getPatientService().getPatientByUuid(fhirPatient.getId());
		NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(patient, patientByUuid.getPatientId());
		nhsPatientService.saveOrUpdate(nhsPatient);
		return nhsPatientMapper.enhance(fhirPatient);
	}
	
	//Todo check if update and create scenarios are working smoothly
	@Override
	public Patient updatePatient(Patient patient, String uuid) {
		Patient fhirPatient = super.updatePatient(patient, uuid);
		
		org.openmrs.Patient patientByUuid = Context.getPatientService().getPatientByUuid(fhirPatient.getId());
		NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(patient, patientByUuid.getPatientId());
		nhsPatientService.saveOrUpdate(nhsPatient);
		return nhsPatientMapper.enhance(fhirPatient);
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
	
	private UnprocessableEntityException createMissingIdentifierPartException(String identifier) {
		String errorMessage = String.format(
		    "One or both of the identifier system and value are missing from given identifier : %s", identifier);
		Coding coding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "INVALID_PARAMETER", "INVALID_PARAMETER");
		OperationOutcome operationOutcome = createErrorOperationOutcome(errorMessage, coding,
		    OperationOutcome.IssueType.INVALID);
		return new UnprocessableEntityException(errorMessage, operationOutcome);
	}
	
}

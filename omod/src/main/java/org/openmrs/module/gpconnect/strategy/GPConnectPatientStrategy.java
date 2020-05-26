package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.fhir.api.strategies.patient.PatientStrategy;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
		return super.searchPatientsByIdentifier(identifierValue, identifierTypeName).stream()
				.map(patient -> nhsPatientMapper.enhance(patient))
				.collect(Collectors.toList());
	}

	@Override
	public List<Patient> searchPatientsByIdentifier(String identifier) {
		return super.searchPatientsByIdentifier(identifier)
				.stream()
				.map(patient -> nhsPatientMapper.enhance(patient))
				.collect(Collectors.toList());
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
		NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(fhirPatient, patientByUuid.getPatientId());
		nhsPatientService.save(nhsPatient);
		return nhsPatientMapper.enhance(fhirPatient);
	}

	@Override
	public Patient updatePatient(Patient patient, String uuid) {
		return super.updatePatient(patient, uuid);
	}
}

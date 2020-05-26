package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.fhir.api.strategies.patient.PatientStrategy;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component("GPConnectPatientStrategy")
public class GPConnectPatientStrategy extends PatientStrategy {
	
	@Autowired
	NhsPatientMapper nhsPatientMapper;
	
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
		return super.searchPatients(active);
	}

	@Override
	public Bundle searchPatientsByGivenName(String givenName) {
		return super.searchPatientsByGivenName(givenName);
	}

	@Override
	public Bundle searchPatientsByFamilyName(String familyName) {
		return super.searchPatientsByFamilyName(familyName);
	}

	@Override
	public Bundle searchPatientsByName(String name) {
		return super.searchPatientsByName(name);
	}

	@Override
	public Bundle getPatientOperationsById(String patientId) {
		return super.getPatientOperationsById(patientId);
	}

	@Override
	public void deletePatient(String uuid) {
		super.deletePatient(uuid);
	}

	@Override
	public Patient createFHIRPatient(Patient patient) {
		return super.createFHIRPatient(patient);
	}

	@Override
	public Patient updatePatient(Patient patient, String uuid) {
		return super.updatePatient(patient, uuid);
	}
}

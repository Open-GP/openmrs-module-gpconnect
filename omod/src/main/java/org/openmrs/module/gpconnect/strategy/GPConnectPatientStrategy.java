package org.openmrs.module.gpconnect.strategy;

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

		return nhsPatientMapper.enhance(patient, uuid);
	}

	@Override
	public List<Patient> searchPatientsById(String uuid) {
		List<Patient> patients = super.searchPatientsById(uuid);

		return patients.stream()
				.map(patient -> nhsPatientMapper.enhance(patient, uuid))
				.collect(Collectors.toList());
	}
}

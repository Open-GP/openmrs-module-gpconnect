package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.fhir.api.strategies.patient.PatientStrategy;
import org.openmrs.module.gpconnect.repository.GPConnectPatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("GPConnectPatientStrategy")
public class GPConnectPatientStrategy extends PatientStrategy {
	
	@Autowired
	GPConnectPatientRepository repository;
	
	@Override
	public Patient getPatient(String uuid) {
		Patient patient = new Patient();
		ContactPoint email = new ContactPoint();
		email.setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("test@mail.com");
		patient.addTelecom(email);
		
		System.out.println(repository.count());
		return patient;
	}
}

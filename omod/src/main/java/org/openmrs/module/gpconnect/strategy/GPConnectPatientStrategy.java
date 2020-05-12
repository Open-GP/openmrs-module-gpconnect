package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.fhir.api.strategies.patient.PatientStrategy;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.entity.Person;
import org.openmrs.module.gpconnect.repository.NhsPatientRepository;
import org.openmrs.module.gpconnect.repository.PersonRepository;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("GPConnectPatientStrategy")
public class GPConnectPatientStrategy extends PatientStrategy {
	
	@Autowired
	PersonRepository personRepository;
	
	@Autowired
	NhsPatientRepository nhsPatientRepository;
	
	@Override
	public Patient getPatient(String uuid) {
		Patient patient = super.getPatient(uuid);
		ContactPoint email = new ContactPoint();
		email.setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("test@mail.com");
		patient.addTelecom(email);
		
		Optional<Person> person = personRepository.findByUuid(patient.getId());
		
		if (person.isPresent()) {
			Optional<NhsPatient> nhsPatient = nhsPatientRepository.findById(person.get().id);
			
			if (nhsPatient.isPresent()) {
				Extension cadavericDonor = new Extension(GPConnectExtensions.CADAVERIC_DONOR_URL, new BooleanType(
				        nhsPatient.get().cadavericDonor));
				patient.addExtension(cadavericDonor);
			}
		}
		
		return patient;
	}
}

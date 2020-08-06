package org.openmrs.module.gpconnect.translators;

import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.module.fhir2.api.translators.impl.PatientTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@Component
@Primary
public class PatientTranslator extends PatientTranslatorImpl {
	
	@Override
	public Patient toOpenmrsType(Patient currentPatient, org.hl7.fhir.r4.model.Patient patient) {
		super.toOpenmrsType(currentPatient, patient);
		if (patient.getIdElement().getIdPart() == null) {
			currentPatient.setUuid(UUID.randomUUID().toString());
		} else {
			currentPatient.setUuid(patient.getIdElement().getIdPart());
		}
		if (!patient.hasActive()) {
			currentPatient.setVoided(false);
			currentPatient.setVoidReason(null);
		}
		Set<PersonAttribute> oldAttributes = currentPatient.getAttributes();
		currentPatient.setAttributes(new TreeSet<>());
		for (PersonAttribute personAttribute : oldAttributes) {
			currentPatient.addAttribute(personAttribute);
		}
		return currentPatient;
	}
	
	@Override
	public org.hl7.fhir.r4.model.Patient toFhirResource(Patient openmrsPatient) {
		if (openmrsPatient == null) {
			return null;
		}
		return super.toFhirResource(openmrsPatient);
	}
}

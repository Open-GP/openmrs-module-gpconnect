package org.openmrs.module.gpconnect.translators;

import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.module.fhir2.api.translators.impl.PatientTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.TreeSet;

@Component
@Primary
public class PatientTranslator extends PatientTranslatorImpl {
	
	@Override
	public Patient toOpenmrsType(Patient currentPatient, org.hl7.fhir.r4.model.Patient patient) {
		super.toOpenmrsType(currentPatient, patient);
		currentPatient.setUuid(patient.getIdElement().getIdPart());
		Set<PersonAttribute> oldAttributes = currentPatient.getAttributes();
		currentPatient.setAttributes(new TreeSet<>());
		for (PersonAttribute personAttribute : oldAttributes) {
			currentPatient.addAttribute(personAttribute);
		}
		return currentPatient;
	}
}

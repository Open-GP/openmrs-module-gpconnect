package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.PatientIdentifier;
import org.openmrs.module.fhir2.api.translators.impl.PatientIdentifierTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class PatientIdentifierTranslator extends PatientIdentifierTranslatorImpl {
	
	@Override
	public PatientIdentifier toOpenmrsType(PatientIdentifier patientIdentifier, Identifier identifier) {
		super.toOpenmrsType(patientIdentifier, identifier);
		if (patientIdentifier.getUuid() == null) {
			patientIdentifier.setUuid(UUID.randomUUID().toString());
		}
		return patientIdentifier;
	}
}

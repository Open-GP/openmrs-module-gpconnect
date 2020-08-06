package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.translators.impl.PatientIdentifierTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class PatientIdentifierTranslator extends PatientIdentifierTranslatorImpl {
	
	@Override
	public PatientIdentifier toOpenmrsType(PatientIdentifier patientIdentifier, Identifier identifier) {
		identifier.setType(new CodeableConcept().setText(identifier.getSystem()));
		super.toOpenmrsType(patientIdentifier, identifier);
		if (patientIdentifier.getUuid() == null) {
			patientIdentifier.setUuid(UUID.randomUUID().toString());
		}

		return patientIdentifier;
	}

	@Override
	public Identifier toFhirResource(PatientIdentifier identifier){
		Identifier patientIdentifier = super.toFhirResource(identifier);
		if (identifier.getIdentifierType() != null) {
			patientIdentifier.setSystem(identifier.getIdentifierType().getName());
		}
		return patientIdentifier;
	}
}

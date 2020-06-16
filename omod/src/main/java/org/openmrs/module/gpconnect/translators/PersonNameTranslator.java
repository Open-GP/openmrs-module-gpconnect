package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.HumanName;
import org.openmrs.PersonName;
import org.openmrs.module.fhir2.api.translators.impl.PersonNameTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class PersonNameTranslator extends PersonNameTranslatorImpl {
	
	@Override
	public PersonName toOpenmrsType(PersonName personName, HumanName name) {
		super.toOpenmrsType(personName, name);
		if (personName.getUuid() == null) {
			personName.setUuid(UUID.randomUUID().toString());
		}
		return personName;
	}
}

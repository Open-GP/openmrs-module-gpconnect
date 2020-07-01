package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Address;
import org.openmrs.PersonAddress;
import org.openmrs.module.fhir2.api.translators.impl.PersonAddressTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class PersonAddressTranslator extends PersonAddressTranslatorImpl {
	
	@Override
	public PersonAddress toOpenmrsType(PersonAddress personAddress, Address address) {
		super.toOpenmrsType(personAddress, address);
		if (personAddress.getUuid() == null) {
			personAddress.setUuid(UUID.randomUUID().toString());
		}
		return personAddress;
	}
}

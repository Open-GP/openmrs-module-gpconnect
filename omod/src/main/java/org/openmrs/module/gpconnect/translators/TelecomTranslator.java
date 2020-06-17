package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.fhir2.api.translators.impl.TelecomTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class TelecomTranslator extends TelecomTranslatorImpl {
	
	@Override
	public Object toOpenmrsType(Object attribute, ContactPoint contactPoint) {
		super.toOpenmrsType(attribute, contactPoint);
		
		if (((BaseOpenmrsObject) attribute).getUuid() == null) {
			((BaseOpenmrsObject) attribute).setUuid(UUID.randomUUID().toString());
		}
		
		return attribute;
	}
	
	@Override
	public ContactPoint toFhirResource(Object attribute) {
		ContactPoint contactPoint = super.toFhirResource(attribute);
		
		contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
		
		return contactPoint;
	}
}

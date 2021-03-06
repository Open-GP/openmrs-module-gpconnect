package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.ContactPoint;
import org.openmrs.BaseOpenmrsData;
import org.openmrs.BaseOpenmrsObject;
import org.openmrs.module.fhir2.api.translators.impl.TelecomTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Primary
public class TelecomTranslator extends TelecomTranslatorImpl {
	
	@Override
	public BaseOpenmrsData toOpenmrsType(BaseOpenmrsData attribute, ContactPoint contactPoint){
		super.toOpenmrsType(attribute, contactPoint);
		
		if (attribute.getUuid() == null) {
			attribute.setUuid(UUID.randomUUID().toString());
		}
		
		return attribute;
	}
	
	@Override
	public ContactPoint toFhirResource(BaseOpenmrsData attribute) {
		ContactPoint contactPoint = super.toFhirResource(attribute);
		
		contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);
		contactPoint.setUse(ContactPoint.ContactPointUse.HOME);
		
		return contactPoint;
	}
}

package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.impl.PractitionerTranslatorProviderImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class PractitionerTranslator extends PractitionerTranslatorProviderImpl {
	
	@Autowired
	GenderTranslator genderTranslator;
	
	@Autowired
	PersonNameTranslator nameTranslator;
	
	@Override
	public Provider toOpenmrsType(Provider existingProvider, Practitioner practitioner) {
		Provider provider = super.toOpenmrsType(existingProvider, practitioner);
		Person person = new Person();
		
		if (practitioner.getGender() != null) {
			person.setGender(this.genderTranslator.toOpenmrsType(practitioner.getGender()));
		}
		
		for (HumanName name : practitioner.getName()) {
			person.addName(nameTranslator.toOpenmrsType(name));
		}
		
		provider.setPerson(person);
		provider.setUuid(practitioner.getIdElement().getIdPart());
		if (!practitioner.hasActiveElement()) {
			provider.setRetired(false);
			provider.setDateRetired(null);
			provider.setRetireReason(null);
		}
		
		return provider;
	}
	
	@Override
	public Practitioner toFhirResource(Provider provider) {
		if (provider == null) {
			return null;
		}
		Practitioner practitioner = super.toFhirResource(provider);
		practitioner.getIdentifier().get(0).setSystem("https://fhir.nhs.uk/Id/sds-user-id");
		practitioner.setActive(!provider.getRetired());
		return practitioner;
	}
}

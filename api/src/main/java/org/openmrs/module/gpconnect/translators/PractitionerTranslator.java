package org.openmrs.module.gpconnect.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
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

	public static final String FHIR_NHS_SDS_USER_ID_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";
	public static final String FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM = "https://fhir.nhs.uk/Id/sds-role-profile-id";

	@Autowired
	GenderTranslator genderTranslator;
	
	@Autowired
	PersonNameTranslator nameTranslator;

	@Autowired
	SdsRoleProfileIdTranslator sdsRoleProfileIdTranslator;
	
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

		if (practitioner.getIdentifier().isEmpty()) {
			return provider;
		} else if (practitioner.getIdentifier().stream().anyMatch(identifier -> identifier.getSystem().equals(FHIR_NHS_SDS_USER_ID_SYSTEM))) {
			provider.setIdentifier(practitioner.getIdentifier().get(0).getValue());
		}

		List<Identifier> sdsRoleProfileIdentifiers = practitioner.getIdentifier()
			.stream()
			.filter(identifier -> identifier.getSystem().equals(FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM))
			.collect(Collectors.toList());

		sdsRoleProfileIdentifiers.forEach(sdsRoleProfileIdentifier ->
			provider.addAttribute(sdsRoleProfileIdTranslator.toOpenmrsType(sdsRoleProfileIdentifier))
		);

		return provider;
	}
	
	@Override
	public Practitioner toFhirResource(Provider provider) {
		if (provider == null) {
			return null;
		}
		Practitioner practitioner = super.toFhirResource(provider);
		Identifier sdsUserIdIdentifier = new Identifier()
			.setSystem(FHIR_NHS_SDS_USER_ID_SYSTEM)
			.setValue(provider.getIdentifier());
		List<Identifier> identifiers = new ArrayList<Identifier>(){{
			add(sdsUserIdIdentifier);
		}};
		practitioner.setIdentifier(identifiers);

		provider.getAttributes().forEach(providerAttribute ->
			practitioner.addIdentifier(sdsRoleProfileIdTranslator.toFhirResource(providerAttribute))
		);

		practitioner.setActive(!provider.getRetired());

		return practitioner;
	}
}

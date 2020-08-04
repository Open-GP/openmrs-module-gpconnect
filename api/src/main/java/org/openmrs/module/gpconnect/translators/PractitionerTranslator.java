package org.openmrs.module.gpconnect.translators;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.Person;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.api.impl.ProviderServiceImpl;
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
	public static final String SDS_ROLE_PROFILE_ID_PROVIDER_ATTRIBUTE_TYPE_UUID = "e7852971-2ec5-47dd-9961-c68bab71ce82";


	@Autowired
	GenderTranslator genderTranslator;
	
	@Autowired
	PersonNameTranslator nameTranslator;

	@Autowired
	ProviderService providerService;
	
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

		if (practitioner.getIdentifier().stream().anyMatch(identifier -> identifier.getSystem().equals(FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM))) {
			ProviderAttributeType providerAttributeType = providerService.getProviderAttributeTypeByUuid(SDS_ROLE_PROFILE_ID_PROVIDER_ATTRIBUTE_TYPE_UUID);

			List<Identifier> sdsRoleProfileIdentifiers = practitioner.getIdentifier()
				.stream()
				.filter(identifier -> identifier.getSystem().equals(FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM))
				.collect(Collectors.toList());

			for (Identifier sdsRoleProfileIdentifier : sdsRoleProfileIdentifiers) {
				ProviderAttribute providerAttribute = new ProviderAttribute();
				providerAttribute.setProvider(provider);
				providerAttribute.setAttributeType(providerAttributeType);
				providerAttribute.setValueReferenceInternal(sdsRoleProfileIdentifier.getValue());
				provider.addAttribute(providerAttribute);
			}
		}
		
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

		provider.getAttributes().forEach(providerAttribute -> {
				Identifier sdsRoleProfileIdIdentifier = new Identifier()
					.setSystem(FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM)
					.setValue(providerAttribute.getValueReference());
				practitioner.addIdentifier(sdsRoleProfileIdIdentifier);
			}
		);

		practitioner.setActive(!provider.getRetired());

		return practitioner;
	}
}

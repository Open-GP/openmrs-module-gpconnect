package org.openmrs.module.gpconnect.translators;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Set;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Provider;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import org.openmrs.module.fhir2.api.dao.FhirPractitionerDao;
import org.openmrs.module.fhir2.api.translators.GenderTranslator;
import org.openmrs.module.fhir2.api.translators.PersonAddressTranslator;
import org.openmrs.module.fhir2.api.translators.PersonNameTranslator;
import org.openmrs.module.fhir2.api.translators.ProvenanceTranslator;
import org.openmrs.module.fhir2.api.translators.TelecomTranslator;

@RunWith(MockitoJUnitRunner.class)
public class PractitionerTranslatorTest {
    private static final String VALID_SDS_USER_ID_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";
    private static final String VALID_PRACTITIONER_SDS_USER_ID = "G22222226";
    private static final String VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/sds-role-profile-id";
    private static final ArrayList<String> VALID_PRACTITIONER_ROLE_PROFILE_IDS = new ArrayList<String>(){{
        add("PT3333");
        add("PT1122");
    }};

    @Mock
    private PersonNameTranslator personNameTranslator;
    @Mock
    private PersonAddressTranslator personAddressTranslator;
    @Mock
    private GenderTranslator genderTranslator;
    @Mock
    private TelecomTranslator<Object> telecomTranslator;
    @Mock
    private FhirPractitionerDao fhirPractitionerDao;
    @Mock
    private FhirGlobalPropertyService fhirGlobalPropertyService;
    @Mock
    private ProvenanceTranslator<Provider> provenanceTranslator;
    @Mock
    private ProviderService providerService;

    @InjectMocks
    private PractitionerTranslator practitionerTranslator;

    @Test
    public void shouldReturnProviderWithNoSdsRoleProfileIdGivenPractitionerWithNoSdsRoleProfileId() {
        Practitioner practitioner = createPractitionerWithValidIdentifiers(0);
        when(providerService.getProviderAttributeTypeByUuid(any())).thenReturn(new ProviderAttributeType());

        Provider actualProvider = practitionerTranslator.toOpenmrsType(practitioner);
        assertTrue(actualProvider.getAttributes().isEmpty());
    }

    @Test
    public void shouldReturnProviderWithOneSdsRoleProfileIdGivenPractitionerWithOneSdsRoleProfileId() {
        Practitioner practitioner = createPractitionerWithValidIdentifiers(1);
        when(providerService.getProviderAttributeTypeByUuid(any())).thenReturn(new ProviderAttributeType());

        Provider actualProvider = practitionerTranslator.toOpenmrsType(practitioner);

        Set<ProviderAttribute> providerAttributes = actualProvider.getAttributes();

        assertThat(providerAttributes.size(), equalTo(1));

        assertTrue(providerAttributes.stream().anyMatch(attribute ->
            attribute.getValue().equals(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(0)))
        );
    }

    @Test
    public void shouldReturnProviderWithTwoSdsRoleProfileIdsGivenPractitionerWithTwoSdsRoleProfileIds() {
        Practitioner practitioner = createPractitionerWithValidIdentifiers(2);
        when(providerService.getProviderAttributeTypeByUuid(any())).thenReturn(new ProviderAttributeType());

        Provider actualProvider = practitionerTranslator.toOpenmrsType(practitioner);

        Set<ProviderAttribute> providerAttributes = actualProvider.getAttributes();

        assertThat(providerAttributes.size(), equalTo(2));

        assertTrue(providerAttributes.stream().anyMatch(attribute ->
            attribute.getValue().equals(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(0)))
        );
        assertTrue(providerAttributes.stream().anyMatch(attribute ->
            attribute.getValue().equals(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(1)))
        );
    }

    @Test
    public void shouldReturnPractitionerWithNoSdsRoleProfileIdGivenProviderWithNoSdsRoleProfileId() {
        Provider provider = createProvider(0);

        Practitioner actualPractitioner = practitionerTranslator.toFhirResource(provider);

        assertThat(actualPractitioner.getIdentifier().size(), equalTo(1));

        assertTrue(actualPractitioner.getIdentifier().stream().anyMatch(identifier ->
            identifier.getSystem().equals(VALID_SDS_USER_ID_IDENTIFIER_SYSTEM) &&
                identifier.getValue().equals(VALID_PRACTITIONER_SDS_USER_ID))
        );
    }

    @Test
    public void shouldReturnPractitionerWithOneSdsRoleProfileIdGivenProviderWithOneSdsRoleProfileId() {
        Provider provider = createProvider(1);

        Practitioner actualPractitioner = practitionerTranslator.toFhirResource(provider);

        assertThat(actualPractitioner.getIdentifier().size(), equalTo(2));

        assertTrue(actualPractitioner.getIdentifier().stream().anyMatch(identifier ->
            identifier.getSystem().equals(VALID_SDS_USER_ID_IDENTIFIER_SYSTEM) &&
                identifier.getValue().equals(VALID_PRACTITIONER_SDS_USER_ID))
        );
        assertTrue(actualPractitioner.getIdentifier().stream().anyMatch(identifier ->
            identifier.getSystem().equals(VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM) &&
                identifier.getValue().equals(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(0)))
        );
    }

    @Test
    public void shouldReturnPractitionerWithTwoSdsRoleProfileIdsGivenProviderWithTwoSdsRoleProfileIds() {
        Provider provider = createProvider(2);

        Practitioner actualPractitioner = practitionerTranslator.toFhirResource(provider);

        assertThat(actualPractitioner.getIdentifier().size(), equalTo(3));

        assertTrue(actualPractitioner.getIdentifier().stream().anyMatch(identifier ->
            identifier.getSystem().equals(VALID_SDS_USER_ID_IDENTIFIER_SYSTEM) &&
                identifier.getValue().equals(VALID_PRACTITIONER_SDS_USER_ID))
        );
        assertTrue(actualPractitioner.getIdentifier().stream().anyMatch(identifier ->
            identifier.getSystem().equals(VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM) &&
                identifier.getValue().equals(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(0)))
        );
        assertTrue(actualPractitioner.getIdentifier().stream().anyMatch(identifier ->
            identifier.getSystem().equals(VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM) &&
                identifier.getValue().equals(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(1)))
        );
    }

    private Practitioner createPractitionerWithValidIdentifiers(int numberOfSdsProfileIds) {
        Practitioner practitioner = new Practitioner();
        Identifier sdsUserIdIdentifier = new Identifier()
            .setSystem("https://fhir.nhs.uk/Id/sds-user-id")
            .setValue(VALID_PRACTITIONER_SDS_USER_ID);
        practitioner.addIdentifier(sdsUserIdIdentifier);

        for (int i = 0; i < numberOfSdsProfileIds; i++) {
            Identifier sdsRoleProfileIdIdentifier = new Identifier()
                .setSystem("https://fhir.nhs.uk/Id/sds-role-profile-id")
                .setValue(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(i));
            practitioner.addIdentifier(sdsRoleProfileIdIdentifier);
        }

        return practitioner;
    }

    private Provider createProvider(int numberOfSdsProfileIds) {
        Provider provider = new Provider();
        provider.setIdentifier(VALID_PRACTITIONER_SDS_USER_ID);

        ProviderAttributeType providerAttributeType = new ProviderAttributeType();
        providerAttributeType.setName("SDS role profile id");

        for (int i = 0; i < numberOfSdsProfileIds; i++) {
            ProviderAttribute providerAttribute = new ProviderAttribute();
            providerAttribute.setProvider(provider);
            providerAttribute.setAttributeType(providerAttributeType);
            providerAttribute.setValue(VALID_PRACTITIONER_ROLE_PROFILE_IDS.get(i));
            provider.addAttribute(providerAttribute);
        }

        return provider;
    }
}

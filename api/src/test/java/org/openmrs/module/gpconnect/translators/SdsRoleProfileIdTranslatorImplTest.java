package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Identifier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SdsRoleProfileIdTranslatorImplTest {
    private static final String VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/sds-role-profile-id";
    private static final String VALID_PRACTITIONER_ROLE_PROFILE_ID = "PT3333";

    @Mock
    ProviderService providerService;

    @InjectMocks
    SdsRoleProfileIdTranslatorImpl sdsRoleProfileIdTranslator;

    @Test
    public void shouldReturnProviderAttributeWithSdsRoleProfileIdGivenIdentifierWithSdsRoleProfileId() {
        Identifier identifier = new Identifier();
        identifier.setSystem(VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM);
        identifier.setValue(VALID_PRACTITIONER_ROLE_PROFILE_ID);

        ProviderAttributeType providerAttributeType = new ProviderAttributeType();
        providerAttributeType.setName("SDS role profile id");
        when(providerService.getProviderAttributeTypeByUuid(any())).thenReturn(providerAttributeType);

        ProviderAttribute actualProviderAttribute = sdsRoleProfileIdTranslator.toOpenmrsType(identifier);

        assertThat(actualProviderAttribute.getValueReference(), equalTo(VALID_PRACTITIONER_ROLE_PROFILE_ID));
        assertThat(actualProviderAttribute.getAttributeType().getName(), equalTo("SDS role profile id"));
    }

    @Test
    public void shouldReturnIdentifierWithSdsRoleProfileIdGivenProviderAttributeWithSdsRoleProfileId() {
        ProviderAttributeType providerAttributeType = new ProviderAttributeType();
        providerAttributeType.setName("SDS role profile id");

        ProviderAttribute providerAttribute = new ProviderAttribute();
        providerAttribute.setValueReferenceInternal(VALID_PRACTITIONER_ROLE_PROFILE_ID);
        providerAttribute.setAttributeType(providerAttributeType);

        Identifier identifier = sdsRoleProfileIdTranslator.toFhirResource(providerAttribute);

        assertThat(identifier.getValue(), equalTo(VALID_PRACTITIONER_ROLE_PROFILE_ID));
        assertThat(identifier.getSystem(), equalTo(VALID_SDS_ROLE_PROFILE_ID_IDENTIFIER_SYSTEM));
    }
}

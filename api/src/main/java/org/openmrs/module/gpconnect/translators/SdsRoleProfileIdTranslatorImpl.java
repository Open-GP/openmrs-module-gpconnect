package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.ProviderAttribute;
import org.openmrs.ProviderAttributeType;
import org.openmrs.api.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class SdsRoleProfileIdTranslatorImpl implements SdsRoleProfileIdTranslator {
    public static final String SDS_ROLE_PROFILE_ID_PROVIDER_ATTRIBUTE_TYPE_UUID = "e7852971-2ec5-47dd-9961-c68bab71ce82";
    public static final String FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM = "https://fhir.nhs.uk/Id/sds-role-profile-id";

    @Autowired
    ProviderService providerService;

    @Override
    public ProviderAttribute toOpenmrsType(Identifier identifier) {
        ProviderAttributeType providerAttributeType = providerService.getProviderAttributeTypeByUuid(SDS_ROLE_PROFILE_ID_PROVIDER_ATTRIBUTE_TYPE_UUID);
        ProviderAttribute providerAttribute = new ProviderAttribute();
        providerAttribute.setAttributeType(providerAttributeType);
        providerAttribute.setValueReferenceInternal(identifier.getValue());
        return providerAttribute;
    }

    @Override
    public Identifier toFhirResource(ProviderAttribute providerAttribute) {
        return new Identifier()
                .setSystem(FHIR_NHS_SDS_ROLE_PROFILE_ID_SYSTEM)
                .setValue(providerAttribute.getValueReference());
    }
}

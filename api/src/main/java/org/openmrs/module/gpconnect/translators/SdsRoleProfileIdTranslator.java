package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.ProviderAttribute;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;

public interface SdsRoleProfileIdTranslator extends OpenmrsFhirTranslator<ProviderAttribute, Identifier> {
    ProviderAttribute toOpenmrsType(Identifier var1);

    Identifier toFhirResource(ProviderAttribute var1);
}

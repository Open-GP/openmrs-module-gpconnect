package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.mappers.valueSets.EthnicCategory;
import org.openmrs.module.gpconnect.mappers.valueSets.RegistrationType;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.List;
import java.util.Optional;

public enum CodeableConceptExtension{
    ETHNIC_CATEGORY(Extensions.ETHNIC_CATEGORY_URL, CodeSystems.ETHNIC_CATEGORY, EthnicCategory.names()),
    REGISTRATION_TYPE(Extensions.REGISTRATION_TYPE, CodeSystems.REGISTRATION_TYPE, RegistrationType.names()),
    RESIDENTIAL_STATUS(Extensions.RESIDENTIAL_STATUS_URL, CodeSystems.RESIDENTIAL_STATUS, ResidentialStatus.names());

    private final String extensionUrl;
    private final String codeSystem;
    private final List<String> values;

    CodeableConceptExtension(String extensionUrl, String codeSystem, List<String> values) {
        this.extensionUrl = extensionUrl;
        this.codeSystem = codeSystem;
        this.values = values;
    }

    public Optional<String> getValue(Patient patient) {
        return getValue(patient.getExtensionsByUrl(extensionUrl));
    }

    public Optional<String> getValue(Extension extension) {
        return getValue(extension.getExtensionsByUrl(extensionUrl));
    }

    private Optional<String> getValue(List<Extension> extensions) {
        if (extensions.size() > 0) {
            Coding coding = ((CodeableConcept) extensions.get(0).getValue()).getCoding().get(0);

            if (coding.getSystem().equals(codeSystem) && values.contains(coding.getCode())) {
                return Optional.of(coding.getCode());
            }

        }
        return Optional.empty();
    }

}

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
import java.util.Map;
import java.util.Optional;

public enum CodeableConceptExtension {
    ETHNIC_CATEGORY(Extensions.ETHNIC_CATEGORY_URL, CodeSystems.ETHNIC_CATEGORY, EthnicCategory.dict()),
    REGISTRATION_TYPE(Extensions.REGISTRATION_TYPE, CodeSystems.REGISTRATION_TYPE, RegistrationType.dict()),
    RESIDENTIAL_STATUS(Extensions.RESIDENTIAL_STATUS_URL, CodeSystems.RESIDENTIAL_STATUS, ResidentialStatus.dict());

    private final String extensionUrl;
    private final String codeSystem;
    private final Map<String, String> dictionary;

    CodeableConceptExtension(String extensionUrl, String codeSystem, Map<String, String> dictionary) {
        this.extensionUrl = extensionUrl;
        this.codeSystem = codeSystem;
        this.dictionary = dictionary;
    }

    public Optional<Extension> createExtension(String value) {
        if (value == null) {
            return Optional.empty();
        }

        if (!dictionary.containsKey(value)) {
            System.out.printf("The %s: %s is not a known one\n", extensionUrl, value);

            return Optional.empty();
        }

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding(codeSystem, value, dictionary.get(value)));
        Extension extension = new Extension(extensionUrl, codeableConcept);
        return Optional.of(extension);
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

            if (coding.getSystem().equals(codeSystem) && dictionary.containsKey(coding.getCode())) {
                return Optional.of(coding.getCode());
            }

        }
        return Optional.empty();
    }

}
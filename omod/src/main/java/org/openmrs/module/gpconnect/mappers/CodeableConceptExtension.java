package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CodeableConceptExtension {

    private final String extensionUrl;
    private final String codeSystem;
    private final Map<String, String> dictionary;

    public CodeableConceptExtension(String extensionUrl, String codeSystem, Map<String, String> dictionary) {
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


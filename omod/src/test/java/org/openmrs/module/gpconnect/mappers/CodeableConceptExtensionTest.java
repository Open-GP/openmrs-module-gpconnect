package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CodeableConceptExtensionTest {

    CodeableConceptExtension codeableConceptExtension;

    @Before
    public void setUp() {
        HashMap<String, String> dictionary = new HashMap();
        dictionary.put("key", "value");
        codeableConceptExtension = new CodeableConceptExtension("extension-url", "code-system", dictionary);
    }

    @Test
    public void shouldSetResidentialStatus() {
        Optional<Extension> extension = codeableConceptExtension.createExtension("key");

        CodeableConcept codeableConcept = (CodeableConcept) extension.get().getValue();
        List<Coding> coding = codeableConcept.getCoding();
        assertEquals(coding.size(), 1);
        assertEquals(coding.get(0).getCode(), "key");
        assertEquals(coding.get(0).getSystem(), "code-system");
        assertEquals(coding.get(0).getDisplay(), "value");
    }

    @Test
    public void shouldNotSetResidentialStatusWhenUnknown() {
        assertFalse(codeableConceptExtension.createExtension("something else").isPresent());
    }

    @Test
    public void shouldMapResidentialStatus() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("code-system", "key", "value"));
        Extension extension = new Extension("extension-url", codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        assertEquals("key", codeableConceptExtension.getValue(patient).get());
    }

    @Test
    public void shouldSkipResidentialStatusMappingWhenSystemUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("something", "key", "soemthing wrong"));
        Extension extension = new Extension("extension-url", codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        assertFalse(codeableConceptExtension.getValue(patient).isPresent());
    }

    @Test
    public void shouldSkipResidentialStatusMappingWhenCodeUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("code-system", "test", "soemthing wrong"));
        Extension extension = new Extension("extension-url", codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        assertFalse(codeableConceptExtension.getValue(patient).isPresent());
    }

}
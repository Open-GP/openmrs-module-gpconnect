package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CodeableConceptExtensionTest {

    CodeableConceptExtension codeableConceptExtension = new CodeableConceptExtension(CodeableConceptExtension.CodeableConceptExtensionEnum.RESIDENTIAL_STATUS);

    @Test
    public void shouldSetResidentialStatus() {
        Optional<Extension> extension = codeableConceptExtension.createExtension("H");

        CodeableConcept codeableConcept = (CodeableConcept) extension.get().getValue();
        List<Coding> coding = codeableConcept.getCoding();
        assertEquals(coding.size(), 1);
        assertEquals(coding.get(0).getCode(), "H");
        assertEquals(coding.get(0).getSystem(), CodeSystems.RESIDENTIAL_STATUS);
        assertEquals(coding.get(0).getDisplay(), "UK Resident");
    }

    @Test
    public void shouldNotSetResidentialStatusWhenUnknown() {
        assertFalse(codeableConceptExtension.createExtension("something else").isPresent());
    }

    @Test
    public void shouldMapResidentialStatus() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(ResidentialStatus.H.getCoding());
        Extension extension = new Extension(Extensions.RESIDENTIAL_STATUS_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        assertEquals("H", codeableConceptExtension.getValue(patient).get());
    }

    @Test
    public void shouldSkipResidentialStatusMappingWhenSystemUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("something", "test", "soemthing wrong"));
        Extension extension = new Extension(Extensions.RESIDENTIAL_STATUS_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        assertFalse(codeableConceptExtension.getValue(patient).isPresent());
    }

    @Test
    public void shouldSkipResidentialStatusMappingWhenCodeUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding(CodeSystems.RESIDENTIAL_STATUS, "test", "soemthing wrong"));
        Extension extension = new Extension(Extensions.RESIDENTIAL_STATUS_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        assertFalse(codeableConceptExtension.getValue(patient).isPresent());
    }

}
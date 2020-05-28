package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.TreatmentCategory;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TreatmentCategoryMapperTest {

    public static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
    TreatmentCategoryMapper treatmentCategoryMapper = new TreatmentCategoryMapper(new CodeableConceptExtension(Extensions.TREATMENT_CATEGORY_URL, CodeSystems.TREATMENT_CATEGORY, TreatmentCategory.dict()));

    Patient patient = new Patient();

    @Test
    public void shouldSetTreatmentCategory() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.treatmentCategory = "1";

        Patient actualPatient = treatmentCategoryMapper.enhance(patient, nhsPatient);

        Extension extension = actualPatient.getExtensionsByUrl(Extensions.TREATMENT_CATEGORY_URL).get(0);
        CodeableConcept codeableConcept = (CodeableConcept) extension.getValue();
        List<Coding> coding = codeableConcept.getCoding();
        assertEquals(coding.size(), 1);
        assertEquals(coding.get(0).getCode(), "1");
        assertEquals(coding.get(0).getSystem(), CodeSystems.TREATMENT_CATEGORY);
        assertEquals(coding.get(0).getDisplay(), "Exempt from payment - subject to reciprocal health agreement");
    }

    @Test
    public void shouldNotSetTreatmentCategoryWhenUnknown() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.treatmentCategory = "something else";

        Patient actualPatient = treatmentCategoryMapper.enhance(patient, nhsPatient);

        assertEquals(0, actualPatient.getExtensionsByUrl(Extensions.TREATMENT_CATEGORY_URL).size());
    }

    @Test
    public void shouldMapTreatmentCategory() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(TreatmentCategory.ONE.getCoding());
        Extension extension = new Extension(Extensions.TREATMENT_CATEGORY_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setTreatmentCategory("1");

        NhsPatient actualPatient = treatmentCategoryMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);

        assertEquals(nhsPatient, actualPatient);
    }

    @Test
    public void shouldSkipTreatmentCategoryMappingWhenSystemUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("something", "test", "soemthing wrong"));
        Extension extension = new Extension(Extensions.TREATMENT_CATEGORY_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient actualPatient = treatmentCategoryMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);

        assertEquals(EMPTY_NHS_PATIENT, actualPatient);
    }

    @Test
    public void shouldSkipTreatmentCategoryMappingWhenCodeUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding(CodeSystems.TREATMENT_CATEGORY, "test", "soemthing wrong"));
        Extension extension = new Extension(Extensions.TREATMENT_CATEGORY_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient actualPatient = treatmentCategoryMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);

        assertEquals(EMPTY_NHS_PATIENT, actualPatient);
    }


}
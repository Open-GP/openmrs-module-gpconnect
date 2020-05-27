package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.EthnicCategory;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EthnicCategoryMapperTest {
    
    EthnicCategoryMapper ethnicCategoryMapper = new EthnicCategoryMapper();

    Patient patient = new Patient();

    @Test
    public void shouldSetEthnicCategory() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.ethnicCategory = "C";

        Patient actualPatient = ethnicCategoryMapper.enhance(patient, nhsPatient);

        Extension extension = actualPatient.getExtensionsByUrl(GPConnectExtensions.ETHNIC_CATEGORY_URL).get(0);
        CodeableConcept codeableConcept = (CodeableConcept) extension.getValue();
        List<Coding> coding = codeableConcept.getCoding();
        assertEquals(coding.size(), 1);
        assertEquals(coding.get(0).getCode(), "C");
        assertEquals(coding.get(0).getSystem(), CodeSystems.ETHNIC_CATEGORY);
        assertEquals(coding.get(0).getDisplay(), "Any other White background");
    }

    @Test
    public void shouldNotSetEthnicCategoryWhenUnknown() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.ethnicCategory = "something else";

        Patient actualPatient = ethnicCategoryMapper.enhance(patient, nhsPatient);

        assertEquals(0, actualPatient.getExtensionsByUrl(GPConnectExtensions.ETHNIC_CATEGORY_URL).size());
    }

    @Test
    public void shouldMapEthnicCategory() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(EthnicCategory.CT.getCoding());
        Extension extension = new Extension(GPConnectExtensions.ETHNIC_CATEGORY_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setEthnicCategory("CT");

        NhsPatient actualPatient = ethnicCategoryMapper.mapToNhsPatient(patient, nhsPatient);

        assertEquals(nhsPatient, actualPatient);
    }

    @Test
    public void shouldSkipEthnicCategoryMappingWhenSystemUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("something", "test", "soemthing wrong"));
        Extension extension = new Extension(GPConnectExtensions.ETHNIC_CATEGORY_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient nhsPatient = new NhsPatient();

        NhsPatient actualPatient = ethnicCategoryMapper.mapToNhsPatient(patient, nhsPatient);

        assertEquals(nhsPatient, actualPatient);
    }

    @Test
    public void shouldSkipEthnicCategoryMappingWhenCodeUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding(CodeSystems.ETHNIC_CATEGORY, "test", "soemthing wrong"));
        Extension extension = new Extension(GPConnectExtensions.ETHNIC_CATEGORY_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient nhsPatient = new NhsPatient();

        NhsPatient actualPatient = ethnicCategoryMapper.mapToNhsPatient(patient, nhsPatient);

        assertEquals(nhsPatient, actualPatient);
    }

}
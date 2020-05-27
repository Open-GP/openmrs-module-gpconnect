package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ResidentialStatusMapperTest {

    public static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
    ResidentialStatusMapper residentialStatueMapper = new ResidentialStatusMapper();

    Patient patient = new Patient();

    @Test
    public void shouldSetResidentialStatus() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.residentialStatus = "H";

        Patient actualPatient = residentialStatueMapper.enhance(patient, nhsPatient);

        Extension extension = actualPatient.getExtensionsByUrl(GPConnectExtensions.RESIDENTIAL_STATUS_URL).get(0);
        CodeableConcept codeableConcept = (CodeableConcept) extension.getValue();
        List<Coding> coding = codeableConcept.getCoding();
        assertEquals(coding.size(), 1);
        assertEquals(coding.get(0).getCode(), "H");
        assertEquals(coding.get(0).getSystem(), CodeSystems.RESIDENTIAL_STATUS);
        assertEquals(coding.get(0).getDisplay(), "UK Resident");
    }

    @Test
    public void shouldNotSetResidentialStatusWhenUnknown() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.residentialStatus = "something else";

        Patient actualPatient = residentialStatueMapper.enhance(patient, nhsPatient);

        assertEquals(0, actualPatient.getExtensionsByUrl(GPConnectExtensions.RESIDENTIAL_STATUS_URL).size());
    }

    @Test
    public void shouldMapResidentialStatus() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(ResidentialStatus.H.getCoding());
        Extension extension = new Extension(GPConnectExtensions.RESIDENTIAL_STATUS_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setResidentialStatus("H");

        NhsPatient actualPatient = residentialStatueMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);

        assertEquals(nhsPatient, actualPatient);
    }

    @Test
    public void shouldSkipResidentialStatusMappingWhenSystemUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding("something", "test", "soemthing wrong"));
        Extension extension = new Extension(GPConnectExtensions.RESIDENTIAL_STATUS_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient actualPatient = residentialStatueMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);

        assertEquals(EMPTY_NHS_PATIENT, actualPatient);
    }

    @Test
    public void shouldSkipResidentialStatusMappingWhenCodeUnknown() {
        Patient patient = new Patient();

        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(new Coding(CodeSystems.RESIDENTIAL_STATUS, "test", "soemthing wrong"));
        Extension extension = new Extension(GPConnectExtensions.RESIDENTIAL_STATUS_URL, codeableConcept);
        patient.setExtension(Collections.singletonList(extension));

        NhsPatient actualPatient = residentialStatueMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);

        assertEquals(EMPTY_NHS_PATIENT, actualPatient);
    }


}
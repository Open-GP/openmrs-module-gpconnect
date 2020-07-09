package org.openmrs.module.gpconnect.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientProviderTest {
    private static final String VALID_PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
    private static final String INVALID_PATIENT_UUID = "lklkjsdfasd";

    @Mock
    FhirPatientService fhirPatientService;

    @Mock
    NhsPatientMapper nhsPatientMapper;

    @InjectMocks
    private GPConnectPatientProvider gpConnectPatientProvider;

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void shouldGetPatientByIdGivenValidId() {
        org.hl7.fhir.r4.model.Patient r4Patient = new org.hl7.fhir.r4.model.Patient();
        when(fhirPatientService.get(VALID_PATIENT_UUID)).thenReturn(r4Patient);

        Patient r3Patient = new Patient();
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        Patient patient = gpConnectPatientProvider.getPatientById(new IdType(VALID_PATIENT_UUID));

        assertThat(patient, equalTo(r3Patient));
    }

    @Test
    public void shouldGetPatientNotFoundGivenInvalidId() {
        when(fhirPatientService.get(INVALID_PATIENT_UUID)).thenReturn(null);

        try {
            gpConnectPatientProvider.getPatientById(new IdType(INVALID_PATIENT_UUID));
            fail("ResourceNotFoundException expected to be thrown but wasn't");
        } catch (ResourceNotFoundException resourceNotFoundException) {
            OperationOutcome operationOutcome = (OperationOutcome) resourceNotFoundException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("No patient details found for patient ID: Patient/" + INVALID_PATIENT_UUID)
            );
        }
    }
}

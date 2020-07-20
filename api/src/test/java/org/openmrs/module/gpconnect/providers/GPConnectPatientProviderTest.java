package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.DateTimeType;
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
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.GPConnectPatientService;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientProviderTest {
    private static final String VALID_PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
    private static final String INVALID_PATIENT_UUID = "lklkjsdfasd";

    @Mock
    FhirPatientService fhirPatientService;

    @Mock
    NhsPatientMapper nhsPatientMapper;

    @Mock
    GPConnectPatientService gpConnectPatientService;

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
            assertThat(operationOutcome.getIssue().get(0).getDiagnostics(),
                    equalTo("No patient details found for patient ID: Patient/" + INVALID_PATIENT_UUID));
        }
    }

    @Test
    public void searchShouldGetBadRequestTooManyIdentifierParams() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam(null, null);
        identifier.addAnd(new TokenParam());

        try {
            gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null, null, null, null, null,
                    null, null, null, null, null);
            fail("BadRequest expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("Too many indentifiers"));
        }
    }

    @Test
    public void searchShouldGetInvalidParamiterMissingIdentifierTypeName() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam(null, null);

        try {
            gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null, null, null, null, null,
                    null, null, null, null, null);
            fail("Invalid Paramiter expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                    .getOperationOutcome();
            assertThat(operationOutcome.getIssue().get(0).getDiagnostics(),
                    equalTo("One or both of the identifier system and value are missing from given identifier : null"));
        }
    }

    @Test
    public void searchShouldGetInvalidParamiterEmptyIdentifierTypeName() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam("", "Test");
        try {
            gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null, null, null, null, null,
                    null, null, null, null, null);
            fail("Invalid Paramiter expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                    .getOperationOutcome();
            assertThat(operationOutcome.getIssue().get(0).getDiagnostics(),
                    equalTo("One or both of the identifier system and value are missing from given identifier : Test"));
        }
    }

    @Test
    public void searchShouldGetInvalidParamiterEmptyIdentifierValue() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam("Test", "");

        try {
            gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null, null, null, null, null,
                    null, null, null, null, null);
            fail("Invalid Paramiter expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                    .getOperationOutcome();
            assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo(
                    "One or both of the identifier system and value are missing from given identifier : Test|"));
        }
    }

    @Test
    public void searchShouldGetInvalidIdentifier() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam("Test", "Test");

        org.hl7.fhir.r4.model.Identifier r4Identifier = new org.hl7.fhir.r4.model.Identifier();
        when(fhirPatientService.getPatientIdentifierTypeByIdentifier(r4Identifier)).thenReturn(null);

        try {
            gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null, null, null, null, null,
                    null, null, null, null, null);
            fail("Invalid Identifier expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertThat(operationOutcome.getIssue().get(0).getDiagnostics(),
                    equalTo("The given identifier system code (Test) is not an expected code"));
        }
    }

    @Test
    public void shouldReturnOnePatientInSearch() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam("Test", "Test");

        org.hl7.fhir.r4.model.Patient r4Patient = new org.hl7.fhir.r4.model.Patient();

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);

        IBundleProvider provider = mock(IBundleProvider.class);

        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        when(fhirPatientService.getPatientIdentifierTypeByIdentifier(Matchers.any()))
                .thenReturn(new PatientIdentifierType());

        when(fhirPatientService.searchForPatients(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(provider);

        when(provider.getResources(0, 0)).thenReturn(Collections.singletonList(r4Patient));

        IBundleProvider resource = gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null,
                null, null, null, null, null, null, null, null, null);

        assertThat(resource.size(), equalTo(1));
    }

    @Test
    public void shouldNotReturnDeadPatientInSearch() {
        TokenAndListParam identifier = generateIdentifierWithTokenParam("Test", "Test");

        org.hl7.fhir.r4.model.Patient r4Patient = new org.hl7.fhir.r4.model.Patient();

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);
        r3Patient.setDeceased(new DateTimeType());

        IBundleProvider provider = mock(IBundleProvider.class);

        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        when(fhirPatientService.getPatientIdentifierTypeByIdentifier(Matchers.any()))
                .thenReturn(new PatientIdentifierType());

        when(fhirPatientService.searchForPatients(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(provider);

        when(provider.getResources(0, 0)).thenReturn(Collections.singletonList(r4Patient));

        IBundleProvider resource = gpConnectPatientProvider.searchPatients(null, null, null, identifier, null, null,
                null, null, null, null, null, null, null, null, null);

        assertThat(resource.size(), equalTo(0));
    }

    @Test
    public void shouldRegisterPatient() {
        Patient expectedPatient = new Patient();
        Patient patientToBeRegistered = new Patient();
        org.openmrs.Patient savedOmrsPatient = new org.openmrs.Patient();
        savedOmrsPatient.setUuid("abcd");

        when(gpConnectPatientService.save(patientToBeRegistered)).thenReturn(savedOmrsPatient);
        when(fhirPatientService.get(Matchers.any())).thenReturn(new org.hl7.fhir.r4.model.Patient());
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(expectedPatient);

        Bundle result = gpConnectPatientProvider.registerPatient(patientToBeRegistered);
        Patient returnedPatient = (Patient) result.getEntry().get(0).getResource();
        assertThat(returnedPatient, equalTo(expectedPatient));
    }

    private TokenAndListParam generateIdentifierWithTokenParam(String system, String value) {
        TokenAndListParam identifier = new TokenAndListParam();
        TokenParam tokenParam = new TokenParam();
        tokenParam.setSystem(system);
        tokenParam.setValue(value);
        identifier.addAnd(tokenParam);
        return identifier;
    }
}

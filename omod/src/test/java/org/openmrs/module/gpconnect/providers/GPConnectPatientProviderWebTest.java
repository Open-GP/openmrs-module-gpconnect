package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.DateTimeType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.GPConnectPatientService;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GPConnectPatientProvider, Patient> {

    private static final String VALID_PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
    private static final String INVALID_PATIENT_UUID = "lklkjsdfasd";

    @Mock
    FhirPatientService patientService;

    @Mock
    NhsPatientMapper nhsPatientMapper;

    @Mock
    GPConnectPatientService gpConnectPatientService;

    @Getter(AccessLevel.PUBLIC)
    @InjectMocks
    private GPConnectPatientProvider resourceProvider;

    @Test
    public void shouldGetPatientByUuid() throws Exception {
        org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
        when(patientService.get(VALID_PATIENT_UUID)).thenReturn(patient);

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        MockHttpServletResponse response = get("/Patient/" + VALID_PATIENT_UUID).accept(FhirMediaTypes.JSON).go();

        verify(nhsPatientMapper, atLeastOnce()).enhance(Matchers.any());

        assertThat(response, isOk());
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        Patient resource = readResponse(response);
        assertThat(resource.getIdElement().getIdPart(), equalTo(VALID_PATIENT_UUID));
    }

    @Test
    public void shouldGetPatientByUuidWithAnyInteractionId() throws Exception {
        org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
        when(patientService.get(VALID_PATIENT_UUID)).thenReturn(patient);

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        MockHttpServletResponse response = get("/Patient/" + VALID_PATIENT_UUID)
            .setInteractionId("SomeInteractionId")
            .accept(FhirMediaTypes.JSON).go();

        verify(nhsPatientMapper, atLeastOnce()).enhance(Matchers.any());

        assertThat(response, statusEquals(200));
    }

    @Test
    public void shouldGetPatientNotFoundGivenInvalidUuid() throws IOException, ServletException {
        when(patientService.get(INVALID_PATIENT_UUID)).thenReturn(null);

        MockHttpServletResponse response = get("/Patient/" + INVALID_PATIENT_UUID).accept(FhirMediaTypes.JSON).go();

        verify(nhsPatientMapper, never()).enhance(Matchers.any());

        assertThat(response, statusEquals(404));
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("No patient details found for patient ID: Patient/" + INVALID_PATIENT_UUID));
    }

    @Test
    public void shouldReturn409WhenNhsNumberExists() throws IOException, ServletException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("patientRegister.json");
         String patientRegisterTemplate =  IOUtils.toString(is, StandardCharsets.UTF_8.name());

        String nhsNumber = "1234567890";

        String patientRegister = patientRegisterTemplate.replace("$$nhsNumber$$", nhsNumber);

        when(gpConnectPatientService.save(Matchers.any())).thenThrow(new ResourceVersionConflictException("Nhs number exists"));

        MockHttpServletResponse response = post("/Patient/$gpc.registerpatient").jsonContent(patientRegister).go();

        assertThat(response, statusEquals(409));
    }

    @Test
    public void shouldReturnValidPatientInSearch() throws IOException, ServletException {

        org.hl7.fhir.r4.model.Patient r4Patient = new org.hl7.fhir.r4.model.Patient();

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);

        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);
        
        when(patientService.getPatientIdentifierTypeByIdentifier(Matchers.any()))
            .thenReturn(new PatientIdentifierType());
        
        IBundleProvider provider = mock(IBundleProvider.class);
        when(provider.getResources(0, 0)).thenReturn(Collections.singletonList(r4Patient));

        when(patientService.searchForPatients(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),
            Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),
            Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any()
        )).thenReturn(provider);

        MockHttpServletResponse response = get("/Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|1234567890").go();

        assertThat(response, statusEquals(200));
        Bundle resource = readBundleResponse(response);

        assertThat(resource.getEntry().size(), equalTo(1));
    }

    @Test
    public void shouldSkipDeadPatientsInSearch() throws IOException, ServletException {

        org.hl7.fhir.r4.model.Patient inactivePatient = new org.hl7.fhir.r4.model.Patient();

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);
        r3Patient.setDeceased(new DateTimeType());

        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);
        
        when(patientService.getPatientIdentifierTypeByIdentifier(Matchers.any()))
            .thenReturn(new PatientIdentifierType());
        
        IBundleProvider provider = mock(IBundleProvider.class);
        when(provider.getResources(0, 0)).thenReturn(Collections.singletonList(inactivePatient));

        when(patientService.searchForPatients(Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),
            Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),
            Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any(),Matchers.any()
        )).thenReturn(provider);

        MockHttpServletResponse response = get("/Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|1234567890").go();

        assertThat(response, statusEquals(200));
        Bundle resource = readBundleResponse(response);

        assertThat(resource.getEntry().size(), equalTo(0));
    }

    @Test
    public void shouldThrowAppropriateExceptionForInvalidUrlParameters() throws Exception {

        MockHttpServletResponse response = get("/Patient?Identifier=https://fhir.nhs.uk/Id/nhs-number|1234567890").go();

        OperationOutcome resource = (OperationOutcome) readOperationOutcomeResponse(response);
        List<OperationOutcomeIssueComponent> issues = resource.getIssue();

        assertThat(response, statusEquals(400));
        assertTrue(resource.hasMeta());
        assertThat(issues.size(), greaterThanOrEqualTo(1));

        for (OperationOutcomeIssueComponent issue : issues) {
            Coding expectedCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "BAD_REQUEST", "Bad request");
            List<Coding> coding = issue.getDetails().getCoding();

            assertEquals(issue.getSeverity(), OperationOutcome.IssueSeverity.ERROR);
            assertTrue(issue.hasCode());
            assertThat(coding.size(), equalTo(1));
            assertTrue(coding.get(0).hasCode());
            assertTrue(coding.get(0).hasDisplay());
            assertEquals(expectedCoding.getCode(), coding.get(0).getCode());
        }
    }

    @Test
    public void shouldThrowAppropriateExceptionForMultipleIdentifierParameters() throws Exception {

        MockHttpServletResponse response = get("/Patient?identifier=https://fhir.nhs.uk/Id/nhs-number|1234567890&identifier=https://fhir.nhs.uk/Id/nhs-number|1234567890").go();

        OperationOutcome resource = (OperationOutcome) readOperationOutcomeResponse(response);
        List<OperationOutcomeIssueComponent> issues = resource.getIssue();

        assertThat(response, statusEquals(400));
        assertTrue(resource.hasMeta());
        assertThat(issues.size(), greaterThanOrEqualTo(1));

        for (OperationOutcomeIssueComponent issue : issues) {
            Coding expectedCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "BAD_REQUEST", "BAD_REQUEST");
            List<Coding> coding = issue.getDetails().getCoding();

            assertEquals(issue.getSeverity(), OperationOutcome.IssueSeverity.ERROR);
            assertTrue(issue.hasCode());
            assertThat(coding.size(), equalTo(1));
            assertTrue(coding.get(0).hasCode());
            assertTrue(coding.get(0).hasDisplay());
            assertEquals(expectedCoding.getCode(), coding.get(0).getCode());
        }
    }

    @Test
    public void shouldThrowAppropriateExceptionNoParameters() throws Exception {

        MockHttpServletResponse response = get("/Patient").go();

        OperationOutcome resource = (OperationOutcome) readOperationOutcomeResponse(response);
        List<OperationOutcomeIssueComponent> issues = resource.getIssue();

        assertThat(response, statusEquals(400));
        assertTrue(resource.hasMeta());
        assertThat(issues.size(), greaterThanOrEqualTo(1));

        for (OperationOutcomeIssueComponent issue : issues) {
            Coding expectedCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "BAD_REQUEST", "BAD_REQUEST");
            List<Coding> coding = issue.getDetails().getCoding();

            assertEquals(issue.getSeverity(), OperationOutcome.IssueSeverity.ERROR);
            assertTrue(issue.hasCode());
            assertThat(coding.size(), equalTo(1));
            assertTrue(coding.get(0).hasCode());
            assertTrue(coding.get(0).hasDisplay());
            assertEquals(expectedCoding.getCode(), coding.get(0).getCode());
        }
    }

    @Test
    public void shouldReturn400IfTheInteractionIdResourceDoesNotMatchThePatientResourceInTheUrl() throws IOException, ServletException {
        org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
        when(patientService.get(VALID_PATIENT_UUID)).thenReturn(patient);

        Patient r3Patient = new Patient();
        r3Patient.setId(VALID_PATIENT_UUID);
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        MockHttpServletResponse response = get("/Patient/")
            .accept(FhirMediaTypes.JSON)
            .go();

        assertThat(response, isBadRequest());

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(),
            equalTo("Searching without any parameters is not possible"));
    }
}

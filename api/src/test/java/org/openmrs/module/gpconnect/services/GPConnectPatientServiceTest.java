package org.openmrs.module.gpconnect.services;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

import org.hl7.fhir.dstu3.model.Patient.AnimalComponent;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
import org.hl7.fhir.dstu3.model.Attachment;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientServiceTest {
    private static final String INVALID_NHS_NUMBER = "123456789";
    private static final String VALID_NHS_NUMBER = "1234567890";

    @Mock
    FhirPatientDao fhirPatientDao;

    @Mock
    NhsPatientMapper nhsPatientMapper;

    @Mock
    FhirPatientService fhirPatientService;

    @Mock
    NhsPatientService nhsPatientService;

    @InjectMocks
    GPConnectPatientService gpConnectPatientService;

    @Test
    public void shouldSavePatientIntoDB() {
        int patientId = 987;

        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        org.openmrs.Patient createdPatient = new org.openmrs.Patient();
        createdPatient.setPatientId(patientId);

        when(fhirPatientDao.search(Matchers.any(), Matchers.any())).thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(createdPatient));

        NhsPatient expectedNhsPatient = new NhsPatient();
        when(nhsPatientMapper.toNhsPatient(patient, patientId)).thenReturn(expectedNhsPatient);

        org.openmrs.Patient savedPatient = gpConnectPatientService.save(patient);

        verify(fhirPatientService).create(Matchers.any());

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor
                .forClass(SearchParameterMap.class);
        verify(fhirPatientDao, Mockito.times(2)).search(searchParameterMapArgumentCaptor.capture(), Matchers.any());
        SearchParameterMap value = searchParameterMapArgumentCaptor.getValue();
        TokenAndListParam tokenAndListParam = (TokenAndListParam) value.getParameters("identifier.search.handler")
                .get(0).getParam();
        assertNhsNumber(tokenAndListParam, VALID_NHS_NUMBER);

        verify(nhsPatientMapper).toNhsPatient(patient, patientId);
        verify(nhsPatientService).saveOrUpdate(expectedNhsPatient);

        assertThat(savedPatient, equalTo(createdPatient));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithNoNhsNumber() {
        Patient patient = getValidGPConnectPatient("");
        patient.setIdentifier(Collections.emptyList());

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "Patient is missing id", "INVALID_NHS_NUMBER");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringInvalidNhsNumber() {
        Patient patient = getValidGPConnectPatient(INVALID_NHS_NUMBER);

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "NHS Number is invalid", "INVALID_NHS_NUMBER");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringIdentifierWithoutSystem() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.getIdentifier().get(0).setSystem(null);

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "Identifier is missing System", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringMultipleIdentifiersWithoutSystem() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        Identifier identifier = new Identifier();
        identifier.setValue("No system");
        patient.addIdentifier(identifier);

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "Identifier is missing System", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithAnimal() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        AnimalComponent animal = new AnimalComponent();
        animal.setId("dog");
        patient.setAnimal(animal);

        assertGPConnectException(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
                "Not allowed field: Animal", "INVALID_RESOURCE");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithCommunication() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        PatientCommunicationComponent communication = new Patient.PatientCommunicationComponent();
        communication.setId("some communication");
        patient.setCommunication(Collections.singletonList(communication));

        assertGPConnectException(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
        "Not allowed field: Communication", "INVALID_RESOURCE");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithPhoto() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        Attachment attachment = new Attachment();
        attachment.setId("Some photo");
        patient.setPhoto(Collections.singletonList(attachment));

        assertGPConnectException(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
        "Not allowed field: Photo", "INVALID_RESOURCE");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithDeceased() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setDeceased(new BooleanType(true));

        assertGPConnectException(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
        "Not allowed field: Deceased", "INVALID_RESOURCE");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutDob() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setBirthDate(null);

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "Birth date is mandatory", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutOfficialName() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setName(Collections.emptyList());

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "Patient must have an official name containing at least a family name", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutFamilyName() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        HumanName homer = new HumanName().setUse(HumanName.NameUse.OFFICIAL)
                .setGiven(Collections.singletonList(new StringType("Homer")));
        patient.setName(Collections.singletonList(homer));

        assertGPConnectException(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "Patient must have an official name containing at least a family name", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringDuplicatePatient() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        when(fhirPatientDao.search(Matchers.any(), Matchers.any()))
                .thenReturn(Collections.singletonList(new org.openmrs.Patient()));

        assertGPConnectException(() -> gpConnectPatientService.save(patient), ResourceVersionConflictException.class,
                "Nhs Number already in use", "DUPLICATE_REJECTED");

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor
                .forClass(SearchParameterMap.class);
        verify(fhirPatientDao).search(searchParameterMapArgumentCaptor.capture(), Matchers.any());
        SearchParameterMap value = searchParameterMapArgumentCaptor.getValue();
        TokenAndListParam tokenAndListParam = (TokenAndListParam) value.getParameters("identifier.search.handler")
                .get(0).getParam();
        assertNhsNumber(tokenAndListParam, VALID_NHS_NUMBER);
    }

    private void assertNhsNumber(TokenAndListParam tokenAndListParam, String nhsNumber) {
        TokenParam tokenParam = tokenAndListParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
        assertThat(tokenParam.getSystem(), equalTo(Extensions.NHS_NUMBER_SYSTEM));
        assertThat(tokenParam.getValue(), equalTo(nhsNumber));
    }

    private void assertOperationOutcome(OperationOutcome operationOutcome, String diagnostics, String detailsCodingCode) {
        OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.getIssue().get(0);
        assertThat(issue.getDiagnostics(), equalTo(diagnostics));
        assertThat(issue.getDetails().getCoding().get(0).getCode(), equalTo(detailsCodingCode));
    }

    private void assertGPConnectException(Runnable action, Class exceptionClass, String diagnostics,
            String detailsCodingCode) {
        try {
            action.run();
            fail("Invalid Identifier expected to be thrown but wasn't");
        } catch (final BaseServerResponseException exception) {
            assertTrue(exceptionClass.isInstance(exception));
            final OperationOutcome operationOutcome = (OperationOutcome) exception.getOperationOutcome();
            assertOperationOutcome(operationOutcome, diagnostics, detailsCodingCode);
        }
    }

    private Patient getValidGPConnectPatient(String nhsNumber) {
        Patient patient = new Patient();
        patient.setBirthDate(new Date());

        HumanName humanName = new HumanName();
        humanName.setGiven(Collections.singletonList(new StringType("Homer")));
        humanName.setFamily("Simpson");
        humanName.setUse(HumanName.NameUse.OFFICIAL);
        patient.setName(Collections.singletonList(humanName));

        Identifier identifier = new Identifier();
        identifier.setValue(nhsNumber);
        identifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
        patient.addIdentifier(identifier);
        return patient;
    }
}

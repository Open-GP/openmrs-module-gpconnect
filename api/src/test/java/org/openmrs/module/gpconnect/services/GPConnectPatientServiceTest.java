package org.openmrs.module.gpconnect.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmrs.module.gpconnect.GPConnectTestHelper.assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.Collections;
import java.util.Date;

import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.hl7.fhir.dstu3.model.Patient.AnimalComponent;
import org.hl7.fhir.dstu3.model.Patient.PatientCommunicationComponent;
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

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientServiceTest {
    private static final String INVALID_NHS_NUMBER = "123456789";
    private static final String VALID_NHS_NUMBER = "1234567890";

    @Mock
    private FhirPatientDao fhirPatientDao;

    @Mock
    private NhsPatientMapper nhsPatientMapper;

    @Mock
    private FhirPatientService fhirPatientService;

    @Mock
    private NhsPatientService nhsPatientService;

    @InjectMocks
    private GPConnectPatientService gpConnectPatientService;

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

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "INVALID_NHS_NUMBER", "NHS number invalid", IssueType.VALUE, "Patient is missing id");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringInvalidNhsNumber() {
        Patient patient = getValidGPConnectPatient(INVALID_NHS_NUMBER);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "INVALID_NHS_NUMBER", "NHS number invalid", IssueType.VALUE, "NHS Number is invalid");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringIdentifierWithoutSystem() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.getIdentifier().get(0).setSystem(null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "BAD_REQUEST", "Bad request", IssueType.INVALID, "Identifier is missing System");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringMultipleIdentifiersWithoutSystem() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        Identifier identifier = new Identifier();
        identifier.setValue("No system");
        patient.addIdentifier(identifier);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "BAD_REQUEST", "Bad request", IssueType.INVALID, "Identifier is missing System");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithAnimal() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        AnimalComponent animal = new AnimalComponent();
        animal.setId("dog");
        patient.setAnimal(animal);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
            "INVALID_RESOURCE", "Submitted resource is not valid.", IssueType.INVALID, "Not allowed field: Animal");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithCommunication() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        PatientCommunicationComponent communication = new Patient.PatientCommunicationComponent();
        communication.setId("some communication");
        patient.setCommunication(Collections.singletonList(communication));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
            "INVALID_RESOURCE", "Submitted resource is not valid.", IssueType.INVALID, "Not allowed field: Communication");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithPhoto() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        Attachment attachment = new Attachment();
        attachment.setId("Some photo");
        patient.setPhoto(Collections.singletonList(attachment));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
            "INVALID_RESOURCE", "Submitted resource is not valid.", IssueType.INVALID, "Not allowed field: Photo");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithInvalidTelecom() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        addPatientTelecom(patient,ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, "07923456789");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", IssueType.INVALID, "Invalid telecom. Duplicate use of: Mobile");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithMultipleInvalidTelecom() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        addPatientTelecom(patient,ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.MOBILE, "07923456789");
        addPatientTelecom(patient,ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, "07923456781");
        addPatientTelecom(patient,ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.HOME, "07923456782");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
                "BAD_REQUEST", "Bad request", IssueType.INVALID, "Invalid telecom. Duplicate use of: Mobile, Home");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithDeceased() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setDeceased(new BooleanType(true));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), UnprocessableEntityException.class,
            "INVALID_RESOURCE", "Submitted resource is not valid.", IssueType.INVALID, "Not allowed field: Deceased");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutDob() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setBirthDate(null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "BAD_REQUEST", "Bad request", IssueType.INVALID, "Birth date is mandatory");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutOfficialName() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setName(Collections.emptyList());

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "BAD_REQUEST", "Bad request", IssueType.INVALID, "Patient must have an official name containing at least a family name");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutFamilyName() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        HumanName homer = new HumanName().setUse(HumanName.NameUse.OFFICIAL)
                .setGiven(Collections.singletonList(new StringType("Homer")));
        patient.setName(Collections.singletonList(homer));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), InvalidRequestException.class,
            "BAD_REQUEST", "Bad request", IssueType.INVALID, "Patient must have an official name containing at least a family name");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringDuplicatePatient() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        when(fhirPatientDao.search(Matchers.any(), Matchers.any()))
                .thenReturn(Collections.singletonList(new org.openmrs.Patient()));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient), ResourceVersionConflictException.class,
            "DUPLICATE_REJECTED", "Create would lead to creation of a duplicate resource", IssueType.DUPLICATE, "Nhs Number already in use");

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor
                .forClass(SearchParameterMap.class);
        verify(fhirPatientDao).search(searchParameterMapArgumentCaptor.capture(), Matchers.any());
        SearchParameterMap value = searchParameterMapArgumentCaptor.getValue();
        TokenAndListParam tokenAndListParam = (TokenAndListParam) value.getParameters("identifier.search.handler")
                .get(0).getParam();
        assertNhsNumber(tokenAndListParam, VALID_NHS_NUMBER);
    }

    private void addPatientTelecom(Patient patient, ContactPoint.ContactPointSystem contactPointSystem, ContactPoint.ContactPointUse contactPointUse, String contactValue) {
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(contactPointSystem);
        contactPoint.setValue(contactValue);
        contactPoint.setUse(contactPointUse);
        patient.addTelecom(contactPoint);
    }

    private void assertNhsNumber(TokenAndListParam tokenAndListParam, String nhsNumber) {
        TokenParam tokenParam = tokenAndListParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
        assertThat(tokenParam.getSystem(), equalTo(Extensions.NHS_NUMBER_SYSTEM));
        assertThat(tokenParam.getValue(), equalTo(nhsNumber));
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

        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setUse(ContactPoint.ContactPointUse.MOBILE);
        contactPoint.setValue("07911133122");
        contactPoint.setSystem(ContactPoint.ContactPointSystem.PHONE);

        patient.addTelecom(contactPoint);

        return patient;
    }
}

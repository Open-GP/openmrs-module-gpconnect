package org.openmrs.module.gpconnect.services;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.openmrs.module.gpconnect.GPConnectTestHelper.assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
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

        when(fhirPatientDao.getSearchResults(Matchers.any(), Matchers.any())).thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(createdPatient));

        NhsPatient expectedNhsPatient = new NhsPatient();
        when(nhsPatientMapper.toNhsPatient(patient, patientId)).thenReturn(expectedNhsPatient);

        org.openmrs.Patient savedPatient = gpConnectPatientService.save(patient,true);

        verify(fhirPatientService).create(Matchers.any());

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor
                .forClass(SearchParameterMap.class);
        verify(fhirPatientDao, Mockito.times(2)).getSearchResults(searchParameterMapArgumentCaptor.capture(), Matchers.any());
        SearchParameterMap value = searchParameterMapArgumentCaptor.getValue();
        TokenAndListParam tokenAndListParam = (TokenAndListParam) value.getParameters("identifier.search.handler")
                .get(0).getParam();
        assertNhsNumber(tokenAndListParam, VALID_NHS_NUMBER);

        verify(nhsPatientMapper).toNhsPatient(patient, patientId);
        verify(nhsPatientService).saveOrUpdate(expectedNhsPatient);

        assertThat(savedPatient, equalTo(createdPatient));
    }

    @Test
    public void shouldAddRegistrationDetailsToNhsPatient() {
        int patientId = 987;

        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        org.openmrs.Patient createdPatient = new org.openmrs.Patient();
        createdPatient.setPatientId(patientId);

        when(fhirPatientDao.getSearchResults(Matchers.any(), Matchers.any())).thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(createdPatient));

        NhsPatient expectedNhsPatient = new NhsPatient();
        expectedNhsPatient.setRegistrationType("T");

        NhsPatient nhsPatient = new NhsPatient();
        when(nhsPatientMapper.toNhsPatient(patient, patientId)).thenReturn(nhsPatient);

        gpConnectPatientService.save(patient,true);

        ArgumentCaptor<NhsPatient> nhsPatientArgumentCaptor = ArgumentCaptor.forClass(NhsPatient.class);
        verify(nhsPatientService, times(1)).saveOrUpdate(nhsPatientArgumentCaptor.capture());

        NhsPatient returnedNhsPatient = nhsPatientArgumentCaptor.getValue();
        assertThat(returnedNhsPatient.registrationType, equalTo(expectedNhsPatient.registrationType));
        assertNotNull(returnedNhsPatient.registrationStart);
        assertNotNull(returnedNhsPatient.registrationEnd);
        assertThat(returnedNhsPatient.registrationEnd, equalTo(DateUtils.addMonths(returnedNhsPatient.registrationStart, 3)));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringDuplicatePatient() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        when(fhirPatientDao.getSearchResults(Matchers.any(), Matchers.any()))
                .thenReturn(Collections.singletonList(new org.openmrs.Patient()));

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() -> gpConnectPatientService.save(patient,true), ResourceVersionConflictException.class,
            "DUPLICATE_REJECTED", "Create would lead to creation of a duplicate resource", IssueType.DUPLICATE, "Nhs Number already in use");

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor
                .forClass(SearchParameterMap.class);
        verify(fhirPatientDao).getSearchResults(searchParameterMapArgumentCaptor.capture(), Matchers.any());
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

        addPatientTelecom(patient,ContactPoint.ContactPointSystem.PHONE, ContactPoint.ContactPointUse.TEMP, "07123446789");
        addPatientTelecom(patient,ContactPoint.ContactPointSystem.EMAIL, ContactPoint.ContactPointUse.TEMP, "hello@temp.com");

        return patient;
    }
}

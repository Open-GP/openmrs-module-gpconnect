package org.openmrs.module.gpconnect.services;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
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
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
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

        when(fhirPatientDao.search(Matchers.any(), Matchers.any()))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.singletonList(createdPatient));

        NhsPatient expectedNhsPatient = new NhsPatient();
        when(nhsPatientMapper.toNhsPatient(patient, patientId)).thenReturn(expectedNhsPatient);

        org.openmrs.Patient savedPatient = gpConnectPatientService.save(patient);

        verify(fhirPatientService).create(Matchers.any());

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor.forClass(SearchParameterMap.class);
        verify(fhirPatientDao, Mockito.times(2)).search(searchParameterMapArgumentCaptor.capture(), Matchers.any());
        SearchParameterMap value = searchParameterMapArgumentCaptor.getValue();
        TokenAndListParam tokenAndListParam = (TokenAndListParam) value.getParameters("identifier.search.handler").get(0).getParam();
        assertNhsNumber(tokenAndListParam, VALID_NHS_NUMBER);

        verify(nhsPatientMapper).toNhsPatient(patient, patientId);
        verify(nhsPatientService).saveOrUpdate(expectedNhsPatient);

        assertThat(savedPatient, equalTo(createdPatient));
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithNoNhsNumber() {
        Patient patient = getValidGPConnectPatient("");
        patient.setIdentifier(Collections.emptyList());

        assertInvalidRequest(() -> gpConnectPatientService.save(patient), "Patient is missing id", "INVALID_NHS_NUMBER");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringInvalidNhsNumber() {
        Patient patient = getValidGPConnectPatient(INVALID_NHS_NUMBER);

        assertInvalidRequest(() -> gpConnectPatientService.save(patient), "NHS Number is invalid", "INVALID_NHS_NUMBER");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutDob() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setBirthDate(null);

        assertInvalidRequest(() -> gpConnectPatientService.save(patient), "Birth date is mandatory", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutOfficialName() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        patient.setName(Collections.emptyList());

        assertInvalidRequest(() -> gpConnectPatientService.save(patient),
                "Patient must have an official name containing at least a family name", "BAD_REQUEST");
    }

    @Test
    public void shouldThrowExceptionWhenRegisteringWithoutFamilyName() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);
        HumanName homer = new HumanName()
                .setUse(HumanName.NameUse.OFFICIAL)
                .setGiven(Collections.singletonList(new StringType("Homer")));
        patient.setName(Collections.singletonList(homer));

        assertInvalidRequest(() -> gpConnectPatientService.save(patient),
                "Patient must have an official name containing at least a family name", "BAD_REQUEST");
    }
    @Test
    public void shouldThrowExceptionWhenRegisteringDuplicatePatient() {
        Patient patient = getValidGPConnectPatient(VALID_NHS_NUMBER);

        when(fhirPatientDao.search(Matchers.any(), Matchers.any()))
                .thenReturn(Collections.singletonList(new org.openmrs.Patient()));

        assertResourceVersionConflict(() -> gpConnectPatientService.save(patient), "Nhs Number already in use", "DUPLICATE_REJECTED");

        ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor = ArgumentCaptor.forClass(SearchParameterMap.class);
        verify(fhirPatientDao).search(searchParameterMapArgumentCaptor.capture(), Matchers.any());
        SearchParameterMap value = searchParameterMapArgumentCaptor.getValue();
        TokenAndListParam tokenAndListParam = (TokenAndListParam) value.getParameters("identifier.search.handler").get(0).getParam();
        assertNhsNumber(tokenAndListParam, VALID_NHS_NUMBER);
    }

    private void assertNhsNumber(TokenAndListParam tokenAndListParam, String nhsNumber) {
        TokenParam tokenParam = tokenAndListParam.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
        assertThat(tokenParam.getSystem(), equalTo(Extensions.NHS_NUMBER_SYSTEM));
        assertThat(tokenParam.getValue(), equalTo(nhsNumber));
    }

    private void assertOperationOutcome(OperationOutcome operationOutcome, String diagnostics, String detailsDisplay) {
        OperationOutcome.OperationOutcomeIssueComponent issue = operationOutcome.getIssue().get(0);
        assertThat(issue.getDiagnostics(), equalTo(diagnostics));
        assertThat(issue.getDetails().getCoding().get(0).getDisplay(), equalTo(detailsDisplay));
    }

    private void assertInvalidRequest(Runnable action, String diagnostics, String detailsDisplay) {
        try {
            action.run();
            fail("Invalid Identifier expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertOperationOutcome(operationOutcome, diagnostics, detailsDisplay);
        }
    }

    private void assertResourceVersionConflict(Runnable action, String diagnostics, String detailsDisplay) {
        try {
            action.run();
            fail("Invalid Identifier expected to be thrown but wasn't");
        } catch (final ResourceVersionConflictException exception) {
            final OperationOutcome operationOutcome = (OperationOutcome) exception.getOperationOutcome();
            assertOperationOutcome(operationOutcome, diagnostics, detailsDisplay);
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
        List<Identifier> identifiers = Collections.singletonList(identifier);
        patient.setIdentifier(identifiers);
        return patient;
    }
}
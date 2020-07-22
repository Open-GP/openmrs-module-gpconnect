package org.openmrs.module.gpconnect.providers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.Collections;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPractitionerProviderTest {
    private static final String VALID_PRACTITIONER_UUID = "99c54405-4d21-4d30-a4b1-35e9bb6dffb3";
    private static final String INVALID_PRACTITIONER_UUID = "sdfgdsfsd";
    private static final String INVALID_PRACTITIONER_SDS_USER_ID = "HELLO1234";
    private static final String VALID_PRACTITIONER_SDS_USER_ID = "G22222226";
    private static final String ANOTHER_VALID_PRACTITIONER_SDS_USER_ID = "G13579135";
    public static final String VALID_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

    @Mock
    private FhirPractitionerService practitionerService;

    @InjectMocks
    private GPConnectPractitionerProvider practitionerProvider;

    @Test
    public void shouldGetPractitionerByIdGivenValidId() {
        org.hl7.fhir.r4.model.Practitioner r4Practitioner = new org.hl7.fhir.r4.model.Practitioner();
        r4Practitioner.setId(new IdType(VALID_PRACTITIONER_UUID));
        when(practitionerService.get(VALID_PRACTITIONER_UUID)).thenReturn(r4Practitioner);

        Practitioner r3Practitioner = new Practitioner();
        r3Practitioner.setId(new IdType(VALID_PRACTITIONER_UUID));

        Practitioner actualPractitioner = practitionerProvider.getPractitionerById(new IdType(VALID_PRACTITIONER_UUID));

        assertThat(actualPractitioner.getId(), equalTo(r3Practitioner.getId()));
    }

    @Test
    public void shouldGetPractitionerNotFoundGivenInvalidId() {
        when(practitionerService.get(INVALID_PRACTITIONER_UUID)).thenReturn(null);

        try {
            practitionerProvider.getPractitionerById(new IdType(INVALID_PRACTITIONER_UUID));
            fail("ResourceNotFoundException expected to be thrown but wasn't");
        } catch (ResourceNotFoundException resourceNotFoundException) {
            OperationOutcome operationOutcome = (OperationOutcome) resourceNotFoundException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("No practitioner details found for practitioner ID: Practitioner/" + INVALID_PRACTITIONER_UUID)
            );
        }
    }

    @Test
    public void shouldReturnEmptyListWhenSearchingWithInvalidPractitionerId() {

        TokenParam tokenParam = new TokenParam(VALID_IDENTIFIER_SYSTEM, INVALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        IBundleProvider bundleProvider = mock(IBundleProvider.class);

        when(practitionerService.searchForPractitioners(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(bundleProvider);

        IBundleProvider resources = practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);

        assertThat(resources.size(), equalTo(0));
    }

    @Test
    public void shouldReturnAPractitionerWhenSearchingWithValidPractitionerId() {

        TokenParam identifierSystemAndValue = new TokenParam(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier = new TokenAndListParam().addAnd(identifierSystemAndValue);

        org.hl7.fhir.r4.model.Practitioner r4Practitioner = new org.hl7.fhir.r4.model.Practitioner();

        IBundleProvider bundleProvider = mock(IBundleProvider.class);

        when(bundleProvider.getResources(0, 0))
            .thenReturn(Collections.singletonList(r4Practitioner));
        when(practitionerService
            .searchForPractitioners(any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any())).thenReturn(bundleProvider);

        IBundleProvider resources = practitionerProvider
            .searchForPractitioners(null, identifier, null, null, null, null, null, null, null,
                null);

        assertThat(resources.size(), equalTo(1));
    }

    @Test
    public void shouldReturn400WhenSearchingWithMoreThanOneIdentifierParameter() {
        TokenParam tokenParam = new TokenParam(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam).addAnd(new TokenParam());

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("InvalidRequestException expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("Exactly 1 identifier needs to be provided"));
        }
    }

    @Test
    public void shouldReturn400WhenSearchingWithMultipleIdentifierValuesSeparatedByAComma() {
        TokenParam tokenParam = new TokenParam(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID + "," + ANOTHER_VALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("InvalidRequestException expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("Multiple values detected for non-repeatable parameter 'identifier'."
                    + "This server is not configured to allow multiple (AND/OR) values for this param."));
        }
    }

    @Test
    public void shouldReturn400WhenSearchingWithMultipleIdentifierValuesSeparatedByAPipe() {
        TokenParam tokenParam = new TokenParam(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID + "|" + ANOTHER_VALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("InvalidRequestException expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : "
                    + VALID_IDENTIFIER_SYSTEM + VALID_PRACTITIONER_SDS_USER_ID + "|" + ANOTHER_VALID_PRACTITIONER_SDS_USER_ID));
        }
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemAndValueAreMissing() {
        TokenParam tokenParam = new TokenParam(null, null);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("UnprocessableEntityException expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                .getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : null|null"));
        }
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemAndValueAreEmpty() {
        TokenParam tokenParam = new TokenParam("", "");
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("UnprocessableEntityException expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                .getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : |"));
        }
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemIsMissing() {
        TokenParam tokenParam = new TokenParam(null, VALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("UnprocessableEntityException expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                .getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : null|" + VALID_PRACTITIONER_SDS_USER_ID));
        }
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemIsEmpty() {
        TokenParam tokenParam = new TokenParam("", null);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("UnprocessableEntityException expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                .getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : |null"));
        }
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierValueIsMissing() {
        TokenParam tokenParam = new TokenParam(VALID_IDENTIFIER_SYSTEM, null);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("UnprocessableEntityException expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                .getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : https://fhir.nhs.uk/Id/sds-user-id|null"));
        }
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierValueIsEmpty() {
        TokenParam tokenParam = new TokenParam(VALID_IDENTIFIER_SYSTEM, "");
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("UnprocessableEntityException expected to be thrown but wasn't");
        } catch (final UnprocessableEntityException unprocessableEntityException) {
            final OperationOutcome operationOutcome = (OperationOutcome) unprocessableEntityException
                .getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("One or both of the identifier system and value are missing from given identifier : https://fhir.nhs.uk/Id/sds-user-id|"));
        }
    }

    @Test
    public void shouldReturn400WhenSearchingGivenIdentifierSystemIsInvalidAndValueIsNotEmpty() {
        TokenParam tokenParam = new TokenParam("Test", "Test");
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        try {
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);
            fail("InvalidRequestException expected to be thrown but wasn't");
        } catch (final InvalidRequestException invalidRequestException) {
            final OperationOutcome operationOutcome = (OperationOutcome) invalidRequestException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("The given identifier system code (Test) is not an expected code"));
        }
    }
}

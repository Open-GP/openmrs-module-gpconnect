package org.openmrs.module.gpconnect.providers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.module.gpconnect.GPConnectTestHelper.assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome;
import static org.openmrs.module.gpconnect.GPConnectTestHelper.generateIdentifier;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import java.util.Collections;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
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
    private static final String VALID_IDENTIFIER_SYSTEM = "https://fhir.nhs.uk/Id/sds-user-id";

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

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.getPractitionerById(new IdType(INVALID_PRACTITIONER_UUID)), ResourceNotFoundException.class,
            "PRACTITIONER_NOT_FOUND", "Practitioner record not found", IssueType.NOTFOUND,
            "No practitioner details found for practitioner ID: Practitioner/" + INVALID_PRACTITIONER_UUID
        );
    }

    @Test
    public void shouldReturnEmptyListWhenSearchingWithInvalidPractitionerId() {
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, INVALID_PRACTITIONER_SDS_USER_ID);

        IBundleProvider bundleProvider = mock(IBundleProvider.class);

        when(practitionerService.searchForPractitioners(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(bundleProvider);

        IBundleProvider resources = practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);

        assertThat(resources.size(), equalTo(0));
    }

    @Test
    public void shouldReturnAPractitionerWhenSearchingWithValidPractitionerId() {
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID);

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
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID).addAnd(new TokenParam());

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
            practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            InvalidRequestException.class, "BAD_REQUEST", "Bad request", IssueType.INVALID,
            "Exactly 1 identifier needs to be provided"
        );
    }

    @Test
    public void shouldReturn400WhenSearchingWithMultipleIdentifierValuesSeparatedByAComma() {
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID + "," + ANOTHER_VALID_PRACTITIONER_SDS_USER_ID);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            InvalidRequestException.class, "INVALID_IDENTIFIER_VALUE", "Invalid identifier value", IssueType.VALUE,
            "Multiple values detected for non-repeatable parameter 'identifier'."
                + "This server is not configured to allow multiple (AND/OR) values for this param."
        );
    }

    @Test
    public void shouldReturn422WhenSearchingWithMultipleIdentifierValuesSeparatedByAPipe() {
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, VALID_PRACTITIONER_SDS_USER_ID + "|" + ANOTHER_VALID_PRACTITIONER_SDS_USER_ID);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_IDENTIFIER_VALUE", "Invalid identifier value", IssueType.VALUE,
            "One or both of the identifier system and value are missing from given identifier : "
                + VALID_IDENTIFIER_SYSTEM + "|" + VALID_PRACTITIONER_SDS_USER_ID + "|" + ANOTHER_VALID_PRACTITIONER_SDS_USER_ID
        );
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemAndValueAreMissing() {
        TokenAndListParam identifier = generateIdentifier(null, null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID,
            "One or both of the identifier system and value are missing from given identifier : null|null"
        );
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemAndValueAreEmpty() {
        TokenAndListParam identifier = generateIdentifier("", "");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID,
            "One or both of the identifier system and value are missing from given identifier : |"
        );
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemIsMissing() {
        TokenAndListParam identifier = generateIdentifier(null, VALID_PRACTITIONER_SDS_USER_ID);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID,
            "One or both of the identifier system and value are missing from given identifier : null|" + VALID_PRACTITIONER_SDS_USER_ID
        );
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierSystemIsEmpty() {
        TokenAndListParam identifier = generateIdentifier("", null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID,
            "One or both of the identifier system and value are missing from given identifier : |null"
        );
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierValueIsMissing() {
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, null);

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID,
            "One or both of the identifier system and value are missing from given identifier : https://fhir.nhs.uk/Id/sds-user-id|null"
        );
    }

    @Test
    public void shouldReturn422WhenSearchingGivenIdentifierValueIsEmpty() {
        TokenAndListParam identifier = generateIdentifier(VALID_IDENTIFIER_SYSTEM, "");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            UnprocessableEntityException.class, "INVALID_PARAMETER", "Submitted parameter is not valid.", IssueType.INVALID,
            "One or both of the identifier system and value are missing from given identifier : https://fhir.nhs.uk/Id/sds-user-id|"
        );
    }

    @Test
    public void shouldReturn400WhenSearchingGivenIdentifierSystemIsInvalidAndValueIsNotEmpty() {
        TokenAndListParam identifier = generateIdentifier("Test", "Test");

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
                practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null),
            InvalidRequestException.class, "INVALID_IDENTIFIER_SYSTEM", "Invalid identifier system", IssueType.VALUE,
            "The given identifier system code (Test) is not an expected code"
        );
    }
}

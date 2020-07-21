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
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
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

    @Mock
    FhirPractitionerService practitionerService;

    @InjectMocks
    GPConnectPractitionerProvider practitionerProvider;

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

        TokenParam tokenParam = new TokenParam("https://fhir.nhs.uk/Id/sds-user-id", INVALID_PRACTITIONER_SDS_USER_ID);
        TokenAndListParam identifier =  new TokenAndListParam().addAnd(tokenParam);

        IBundleProvider bundleProvider = mock(IBundleProvider.class);

        when(practitionerService.searchForPractitioners(any(), any(), any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(bundleProvider);

        IBundleProvider resources = practitionerProvider.searchForPractitioners(null, identifier, null, null, null, null, null, null, null, null);

        assertThat(resources.size(), equalTo(0));
    }

    @Test
    public void shouldReturnAPractitionerWhenSearchingWithValidPractitionerId() {

        TokenParam identifierSystemAndValue = new TokenParam("https://fhir.nhs.uk/Id/sds-user-id",
            VALID_PRACTITIONER_SDS_USER_ID);
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
}

package org.openmrs.module.gpconnect.providers;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.BundleProviders;
import java.io.IOException;
import javax.servlet.ServletException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPractitionerProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GPConnectPractitionerProvider, Practitioner> {
    private String VALID_PRACTITIONER_UUID = "1f299464-7cc1-4298-baa7-295cde281ad4";

    @Mock
    private FhirPractitionerService practitionerService;

    @Getter(AccessLevel.PUBLIC)
    @InjectMocks
    GPConnectPractitionerProvider resourceProvider;

    @Test
    public void shouldReturn400IfInteractionIdIsNotPresentInRequestForReadingAPractitioner() throws IOException, ServletException {
        when(practitionerService.get(Matchers.any())).thenReturn(new org.hl7.fhir.r4.model.Practitioner());

        MockHttpServletResponse response = get("/Practitioner/" + VALID_PRACTITIONER_UUID)
            .accept(FhirMediaTypes.JSON)
            .go();

        assertThat(response, isBadRequest());

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("Interaction id does not match resource: Practitioner, action: READ"));
    }

    @Test
    public void shouldReturn400IfInteractionIdStructureDoesNotMatchOneForReadingAPractitioner() throws IOException, ServletException {
        when(practitionerService.get(Matchers.any())).thenReturn(new org.hl7.fhir.r4.model.Practitioner());

        MockHttpServletResponse response = get("/Practitioner/" + VALID_PRACTITIONER_UUID)
            .accept(FhirMediaTypes.JSON)
            .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:location-1")
            .go();

        assertThat(response, isBadRequest());

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(),
            equalTo("Interaction id does not match resource: Practitioner, action: READ"));
    }

    @Test
    public void shouldReturn200IfInteractionIdStructureMatchesOneForReadingAPractitioner() throws IOException, ServletException {
        when(practitionerService.get(Matchers.any())).thenReturn(new org.hl7.fhir.r4.model.Practitioner());

        MockHttpServletResponse response = get("/Practitioner/" + VALID_PRACTITIONER_UUID)
            .accept(FhirMediaTypes.JSON)
            .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:practitioner-1")
            .go();

        assertThat(response, isOk());
    }

    @Test
    public void shouldReturn400IfInteractionIdIsNotPresentInRequestWhenSearchingForAPractitioner() throws IOException, ServletException {
        String identifier = "G11111111";

        IBundleProvider provider = BundleProviders
            .newList(new org.hl7.fhir.r4.model.Practitioner());
        when(practitionerService
            .searchForPractitioners(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any())).thenReturn(provider);

        MockHttpServletResponse response = get(
            "/Practitioner?identifier=https://fhir.nhs.uk/Id/sds-user-id|" + identifier)
            .accept(FhirMediaTypes.JSON)
            .go();

        assertThat(response, isBadRequest());

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("Interaction id does not match resource: Practitioner, action: SEARCH_TYPE"));
    }

    @Test
    public void shouldReturn400IfAnInvalidInteractionIdIsProvidedWhenSearchingForAPractitioner() throws IOException, ServletException {
        String identifier = "G11111111";

        IBundleProvider provider = BundleProviders
            .newList(new org.hl7.fhir.r4.model.Practitioner());
        when(practitionerService
            .searchForPractitioners(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any())).thenReturn(provider);

        MockHttpServletResponse response = get(
            "/Practitioner?identifier=https://fhir.nhs.uk/Id/sds-user-id|" + identifier)
            .accept(FhirMediaTypes.JSON)
            .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:practitioner-1")
            .go();

        assertThat(response, isBadRequest());

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("Interaction id does not match resource: Practitioner, action: SEARCH_TYPE"));
    }

    @Test
    public void shouldReturn400IfAnInvalidParameterIsProvidedWhenSearchingForAPractitioner() throws IOException, ServletException {
        String identifier = "G11111111";

        IBundleProvider provider = BundleProviders
            .newList(new org.hl7.fhir.r4.model.Practitioner());
        when(practitionerService
            .searchForPractitioners(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any())).thenReturn(provider);

        String invalidCaseParameter = "Identifier";
        MockHttpServletResponse response = get(
            "/Practitioner?" + invalidCaseParameter + "=https://fhir.nhs.uk/Id/sds-user-id|" + identifier)
            .accept(FhirMediaTypes.JSON)
            .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:search:practitioner-1")
            .go();

        assertThat(response, isBadRequest());

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("Invalid parameter in request"));
    }

    @Test
    public void shouldReturn200IfValidInteractionIdIsProvidedWhenSearchingForAPractitioner() throws IOException, ServletException {
        String identifier = "G11111111";

        IBundleProvider provider = BundleProviders
            .newList(new org.hl7.fhir.r4.model.Practitioner());
        when(practitionerService
            .searchForPractitioners(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(),
                Matchers.any())).thenReturn(provider);

        MockHttpServletResponse response = get(
            "/Practitioner?identifier=https://fhir.nhs.uk/Id/sds-user-id|" + identifier)
            .accept(FhirMediaTypes.JSON)
            .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:search:practitioner-1")
            .go();

        assertThat(response, isOk());
    }
}

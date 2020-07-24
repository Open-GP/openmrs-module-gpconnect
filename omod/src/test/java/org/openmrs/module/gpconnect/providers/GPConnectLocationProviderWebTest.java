package org.openmrs.module.gpconnect.providers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.ServletException;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.springframework.mock.web.MockHttpServletResponse;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectLocationProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GPConnectLocationProvider, Location> {

    private static final String VALID_LOCATION_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";
    private static final String INVALID_LOCATION_UUID = "lklkjsdfasd";

    @Mock
    FhirLocationService locationService;

    @Getter(AccessLevel.PUBLIC)
    @InjectMocks
    private GPConnectLocationProvider resourceProvider;

    @Test
    public void shouldGetLocationByUuid() throws Exception {
        org.hl7.fhir.r4.model.Location location = new org.hl7.fhir.r4.model.Location();
        location.setId(VALID_LOCATION_UUID);
        when(locationService.get(VALID_LOCATION_UUID)).thenReturn(location);

        MockHttpServletResponse response = get("/Location/" + VALID_LOCATION_UUID).accept(FhirMediaTypes.JSON)
                .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:location-1")
                .go();

        assertThat(response, statusEquals(200));
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        Location resource = readResponse(response);
        assertThat(resource.getIdElement().getIdPart(), equalTo(VALID_LOCATION_UUID));
    }

    @Test
    public void shouldGetLocationNotFoundGivenInvalidUuid() throws IOException, ServletException {
        when(locationService.get(INVALID_LOCATION_UUID)).thenReturn(null);

        MockHttpServletResponse response = get("/Location/" + INVALID_LOCATION_UUID).accept(FhirMediaTypes.JSON)
                .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:location-1")
                .go();

        assertThat(response, statusEquals(404));
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        GPConnectOperationOutcomeTestHelper.assertThatOperationOutcomeHasCorrectStructureAndContent(
            operationOutcome, "LOCATION_NOT_FOUND", "Location record not found", IssueType.NOTFOUND,
            "Could not find location with Id " + INVALID_LOCATION_UUID
        );
    }

    @Test
    public void shouldReturn400IfInteractionIdStructureDoesNotMatchOneForReadingALocation() throws IOException, ServletException {
        when(locationService.get(Matchers.any())).thenReturn(new org.hl7.fhir.r4.model.Location());

        MockHttpServletResponse response = get("/Location/" + VALID_LOCATION_UUID)
                .accept(FhirMediaTypes.JSON)
                .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:practitioner-1")
                .go();

        assertThat(response, statusEquals(400));

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        GPConnectOperationOutcomeTestHelper.assertThatOperationOutcomeHasCorrectStructureAndContent(
            operationOutcome, "BAD_REQUEST", "Bad request", IssueType.INVALID,
            "Interaction id does not match resource: Location, action: READ"
        );
    }

    @Test
    public void shouldReturn200IfInteractionIdStructureMatchesOneForReadingALocation() throws IOException, ServletException {
        when(locationService.get(Matchers.any())).thenReturn(new org.hl7.fhir.r4.model.Location());

        MockHttpServletResponse response = get("/Location/" + VALID_LOCATION_UUID)
                .accept(FhirMediaTypes.JSON)
                .setInteractionId("urn:nhs:names:services:gpconnect:fhir:rest:read:location-1")
                .go();

        assertThat(response, statusEquals(200));
    }
}

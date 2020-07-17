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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
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

        MockHttpServletResponse response = get("/Location/" + VALID_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();

        assertThat(response, isOk());
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        Location resource = readResponse(response);
        assertThat(resource.getIdElement().getIdPart(), equalTo(VALID_LOCATION_UUID));
    }

    @Test
    public void shouldGetLocationNotFoundGivenInvalidUuid() throws IOException, ServletException {
        when(locationService.get(INVALID_LOCATION_UUID)).thenReturn(null);

        MockHttpServletResponse response = get("/Location/" + INVALID_LOCATION_UUID).accept(FhirMediaTypes.JSON).go();

        assertThat(response, isNotFound());
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        OperationOutcome operationOutcome = (OperationOutcome) readOperationOutcomeResponse(response);
        assertThat(operationOutcome.getIssue().get(0).getDiagnostics(), equalTo("Could not find location with Id " + INVALID_LOCATION_UUID));
    }
}

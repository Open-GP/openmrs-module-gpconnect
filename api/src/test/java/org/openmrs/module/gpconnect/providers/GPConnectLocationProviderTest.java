package org.openmrs.module.gpconnect.providers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Location;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirLocationService;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectLocationProviderTest {
    private static final String VALID_LOCATION_UUID = "99c54405-4d21-4d30-a4b1-35e9bb6dffb3";
    private static final String INVALID_LOCATION_UUID = "sdfgdsfsd";

    @Mock
    FhirLocationService locationService;

    @InjectMocks
    GPConnectLocationProvider locationProvider;

    @Test
    public void shouldGetLocationByIdGivenValidId() {
        org.hl7.fhir.r4.model.Location r4Location = new org.hl7.fhir.r4.model.Location();
        r4Location.setId(new IdType(VALID_LOCATION_UUID));
        when(locationService.get(VALID_LOCATION_UUID)).thenReturn(r4Location);

        Location r3Location = new Location();
        r3Location.setId(new IdType(VALID_LOCATION_UUID));

        Location actualLocation = locationProvider.getLocationById(new IdType(VALID_LOCATION_UUID));

        assertThat(actualLocation.getId(), equalTo(r3Location.getId()));
    }

    @Test
    public void shouldGetLocationNotFoundGivenInvalidId() {
        when(locationService.get(INVALID_LOCATION_UUID)).thenReturn(null);

        try {
            locationProvider.getLocationById(new IdType(INVALID_LOCATION_UUID));
            fail("ResourceNotFoundException expected to be thrown but wasn't");
        } catch (ResourceNotFoundException resourceNotFoundException) {
            OperationOutcome operationOutcome = (OperationOutcome) resourceNotFoundException.getOperationOutcome();
            assertThat(
                operationOutcome.getIssue().get(0).getDiagnostics(),
                equalTo("Could not find location with Id " + INVALID_LOCATION_UUID)
            );
        }
    }
}

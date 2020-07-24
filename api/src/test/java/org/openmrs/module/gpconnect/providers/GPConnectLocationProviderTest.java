package org.openmrs.module.gpconnect.providers;


import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.openmrs.module.gpconnect.GPConnectTestHelper.assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.OperationOutcome.IssueType;
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
    private FhirLocationService locationService;

    @InjectMocks
    private GPConnectLocationProvider locationProvider;

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

        assertThatGPConnectExceptionIsThrownWithCorrectOperationOutcome(() ->
            locationProvider.getLocationById(new IdType(INVALID_LOCATION_UUID)), ResourceNotFoundException.class,
            "LOCATION_NOT_FOUND", "Location record not found", IssueType.NOTFOUND,
            "Could not find location with Id " + INVALID_LOCATION_UUID
        );
    }
}

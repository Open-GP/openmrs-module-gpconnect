package org.openmrs.module.gpconnect.providers;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.LOCATION_NOT_FOUND;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Collections;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Location30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Location;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.UriType;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.providers.r3.LocationFhirResourceProvider;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
@Primary
public class GPConnectLocationProvider extends LocationFhirResourceProvider {
	@Autowired
	private FhirLocationService locationService;

	@Operation(name = "$setup")
	public MethodOutcome updateLocation(@OperationParam(name = "location", type = Location.class) Location location) {
		locationService.create(Location30_40.convertLocation(location));
		return new MethodOutcome();
	}

	@Override
	@Read
	public Location getLocationById(@IdParam @NotNull IdType id) {
		try {
			Location location = super.getLocationById(id);
			return addMeta(location);
		} catch(ResourceNotFoundException resourceNotFoundException) {
			throw GPConnectExceptions.resourceNotFoundException("Could not find location with Id " + id.getIdPart(), LOCATION_NOT_FOUND);
		}
	}

	private Location addMeta(Location location) {
		Meta meta = new Meta().setProfile(
		    Collections
		            .singletonList(new UriType("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Location-1")))
		        .setVersionId("1");
		
		location.setMeta(meta);
		return location;
	}
}

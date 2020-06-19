package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Location30_40;
import org.hl7.fhir.dstu3.model.Location;
import org.openmrs.module.fhir2.api.FhirLocationService;
import org.openmrs.module.fhir2.providers.r3.LocationFhirResourceProvider;
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
}

package org.openmrs.module.gpconnect.translators;

import org.openmrs.Location;
import org.openmrs.module.fhir2.api.translators.impl.LocationTranslatorImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class LocationTranslator extends LocationTranslatorImpl {
	
	@Override
	public Location toOpenmrsType(org.hl7.fhir.r4.model.Location fhirLocation) {
		Location location = super.toOpenmrsType(fhirLocation);
		location.setUuid(fhirLocation.getIdElement().getIdPart());
		return location;
	}
	
	@Override
	public org.hl7.fhir.r4.model.Location toFhirResource(Location openmrsLocation) {
		if (openmrsLocation == null) {
			return null;
		}
		
		return super.toFhirResource(openmrsLocation);
	}
}

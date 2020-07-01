package org.openmrs.module.gpconnect.services;

import org.hl7.fhir.r4.model.Organization;
import org.openmrs.module.fhir2.api.FhirService;

public interface FhirOrganisationService extends FhirService<Organization> {
	
	Organization get(String id);
}

package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Organization;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.gpconnect.models.OpenmrsOrganization;
import org.springframework.stereotype.Component;

@Component
public class FhirOrganizationTranslator implements OpenmrsFhirTranslator<OpenmrsOrganization, Organization> {
	
	@Override
	public Organization toFhirResource(OpenmrsOrganization openmrsOrganization) {
		Organization organization = new Organization();
		organization.setId(openmrsOrganization.getUuid());
		organization.setName(openmrsOrganization.getName());
		return organization;
	}
	
	@Override
	public OpenmrsOrganization toOpenmrsType(Organization organization) {
		return null;
	}
}

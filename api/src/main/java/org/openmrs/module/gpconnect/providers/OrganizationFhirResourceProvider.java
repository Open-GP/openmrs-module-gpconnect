package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.convertors.conv30_40.Organization30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Organization;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.gpconnect.services.FhirOrganisationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirR3Resources")
public class OrganizationFhirResourceProvider implements IResourceProvider {
	
	@Autowired
	FhirOrganisationService fhirOrganisationService;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Organization.class;
	}
	
	@Read
	public Organization getById(@IdParam IdType id) {
		org.hl7.fhir.r4.model.Organization organization = fhirOrganisationService.get(id.getIdPart());
		return Organization30_40.convertOrganization(organization);
	}
}

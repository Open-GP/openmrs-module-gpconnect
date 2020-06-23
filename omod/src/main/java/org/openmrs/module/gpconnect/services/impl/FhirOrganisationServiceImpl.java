package org.openmrs.module.gpconnect.services.impl;

import org.hl7.fhir.r4.model.Organization;
import org.openmrs.module.fhir2.api.dao.FhirDao;
import org.openmrs.module.fhir2.api.impl.BaseFhirService;
import org.openmrs.module.fhir2.api.translators.OpenmrsFhirTranslator;
import org.openmrs.module.gpconnect.dao.FhirOrganizationDao;
import org.openmrs.module.gpconnect.models.OpenmrsOrganization;
import org.openmrs.module.gpconnect.services.FhirOrganisationService;
import org.openmrs.module.gpconnect.translators.FhirOrganizationTranslator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class FhirOrganisationServiceImpl extends BaseFhirService<Organization, OpenmrsOrganization> implements FhirOrganisationService {
	
	@Autowired
	FhirOrganizationDao fhirOrganisationDao;
	
	@Autowired
	FhirOrganizationTranslator fhirOrganizationTranslator;
	
	@Override
	public Organization get(String id) {
		OpenmrsOrganization organization = fhirOrganisationDao.get(id);
		return fhirOrganizationTranslator.toFhirResource(organization);
	}
	
	@Override
	protected FhirDao<OpenmrsOrganization> getDao() {
		return fhirOrganisationDao;
	}
	
	@Override
	protected OpenmrsFhirTranslator<OpenmrsOrganization, Organization> getTranslator() {
		return null;
	}
}

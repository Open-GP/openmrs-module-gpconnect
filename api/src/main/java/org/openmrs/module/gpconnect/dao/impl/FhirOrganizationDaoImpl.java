package org.openmrs.module.gpconnect.dao.impl;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.fhir2.api.dao.impl.BaseFhirDao;
import org.openmrs.module.gpconnect.dao.FhirOrganizationDao;
import org.openmrs.module.gpconnect.models.OpenmrsOrganization;
import org.springframework.stereotype.Component;

@Component
public class FhirOrganizationDaoImpl extends BaseFhirDao<OpenmrsOrganization> implements FhirOrganizationDao {
	
	@Override
	public OpenmrsOrganization get(String uuid) {
		return (OpenmrsOrganization) this.getSessionFactory().getCurrentSession().createCriteria(OpenmrsOrganization.class)
		        .add(Restrictions.eq("uuid", uuid)).uniqueResult();
	}
	
	@Override
	public void setSessionFactory(SessionFactory sessionFactory) {
		super.setSessionFactory(sessionFactory);
	}
}

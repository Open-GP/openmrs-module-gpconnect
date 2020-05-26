package org.openmrs.module.gpconnect.dao;

import org.hibernate.SessionFactory;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import static org.hibernate.criterion.Restrictions.eq;

@Component
public class NhsPatientDao {
	
	@Autowired
	@Qualifier("sessionFactory")
	private SessionFactory sessionFactory;
	
	public NhsPatient getPatientById(Long id) {
		return (NhsPatient) sessionFactory.getCurrentSession().createCriteria(NhsPatient.class).add(eq("id", id))
		        .uniqueResult();
	}
	
	public void saveOrUpdate(NhsPatient nhsPatient) {
		sessionFactory.getCurrentSession().saveOrUpdate(nhsPatient);
	}
}

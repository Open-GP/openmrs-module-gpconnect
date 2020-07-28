package org.openmrs.module.gpconnect.dao;

import org.hibernate.SessionFactory;
import org.openmrs.module.gpconnect.entity.NhsPractitioner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.hibernate.criterion.Restrictions.eq;

@Component
@Transactional
public class NhsPractitionerDao {

    @Autowired
    @Qualifier("sessionFactory")
    private SessionFactory sessionFactory;

    public NhsPractitioner getPractitionerById(Long id) {
        return (NhsPractitioner) sessionFactory.getCurrentSession().createCriteria(NhsPractitioner.class).add(eq("id", id))
                .uniqueResult();
    }

    public void saveOrUpdate(NhsPractitioner nhsPractitioner) { sessionFactory.getCurrentSession().saveOrUpdate(nhsPractitioner); }
}


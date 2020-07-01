package org.openmrs.module.gpconnect.services;

import org.openmrs.module.gpconnect.dao.NhsPatientDao;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NhsPatientService {
	
	@Autowired
	NhsPatientDao nhsPatientDao;
	
	public NhsPatient findById(Long id) {
		return nhsPatientDao.getPatientById(id);
	}
	
	public void saveOrUpdate(NhsPatient nhsPatient) {
		nhsPatientDao.saveOrUpdate(nhsPatient);
	}
}

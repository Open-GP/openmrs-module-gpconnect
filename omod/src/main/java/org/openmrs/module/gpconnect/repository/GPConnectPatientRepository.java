package org.openmrs.module.gpconnect.repository;

import org.openmrs.module.gpconnect.entity.GPConnectPatient;
import org.springframework.data.repository.CrudRepository;

public interface GPConnectPatientRepository extends CrudRepository<GPConnectPatient, Long> {
	
}

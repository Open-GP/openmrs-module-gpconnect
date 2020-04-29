package org.openmrs.module.gpconnect.repository;

import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface NhsPatientRepository extends CrudRepository<NhsPatient, Long> {
	
	Optional<NhsPatient> findById(Long id);
}

package org.openmrs.module.gpconnect.repository;

import org.openmrs.module.gpconnect.entity.Person;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PersonRepository extends CrudRepository<Person, Long> {
	
	Optional<Person> findByUuid(String uuid);
}

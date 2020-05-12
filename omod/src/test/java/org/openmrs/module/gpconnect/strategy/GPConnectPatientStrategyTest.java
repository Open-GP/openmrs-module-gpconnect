package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.gpconnect.entity.Person;
import org.openmrs.module.gpconnect.repository.NhsPatientRepository;
import org.openmrs.module.gpconnect.repository.PersonRepository;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GPConnectPatientStrategyTest {
	
	@Mock
	PersonRepository personRepository;
	
	@Mock
	NhsPatientRepository nhsPatientRepository;
	
	@InjectMocks
	GPConnectPatientStrategy patientStrategy;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void shouldMapFakeEmail() {
		String patientUuid = "test";
		
		ServiceContext serviceContext = ServiceContext.getInstance();
		PatientService mockPatientService = mock(PatientService.class);
		when(mockPatientService.getPatientByUuid(patientUuid)).thenReturn(new org.openmrs.Patient());
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
		Person person = new Person();
		when(personRepository.findByUuid(any())).thenReturn(Optional.of(person));
		when(nhsPatientRepository.findById(any())).thenReturn(Optional.empty());
		
		Patient test = patientStrategy.getPatient(patientUuid);
		
		assertEquals(test.getTelecom().get(0).getValue(), "test@mail.com");
	}
}

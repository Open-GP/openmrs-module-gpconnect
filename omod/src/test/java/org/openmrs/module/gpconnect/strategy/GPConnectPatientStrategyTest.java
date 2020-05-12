package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.entity.Person;
import org.openmrs.module.gpconnect.repository.NhsPatientRepository;
import org.openmrs.module.gpconnect.repository.PersonRepository;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;

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
		
		setup(patientUuid);
		
		Person person = new Person();
		when(personRepository.findByUuid(any())).thenReturn(Optional.of(person));
		when(nhsPatientRepository.findById(any())).thenReturn(Optional.empty());
		
		Patient test = patientStrategy.getPatient(patientUuid);
		
		assertEquals(test.getTelecom().get(0).getValue(), "test@mail.com");
	}
	
	private void setup(String patientUuid) {
		ServiceContext serviceContext = ServiceContext.getInstance();
		PatientService mockPatientService = mock(PatientService.class);
		when(mockPatientService.getPatientByUuid(patientUuid)).thenReturn(new org.openmrs.Patient());
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
	}
	
	@Test
	public void shouldSetTheCadavericDonorExtension() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.cadavericDonor = true;
		
		when(personRepository.findByUuid(any())).thenReturn(Optional.of(new Person()));
		when(nhsPatientRepository.findById(any())).thenReturn(Optional.of(nhsPatient));
		
		Patient patient = patientStrategy.getPatient(patientUuid);
		
		Extension extension = patient.getExtensionsByUrl(GPConnectExtensions.CADAVERIC_DONOR_URL).get(0);
		assertEquals(((BooleanType) extension.getValue()).booleanValue(), true);
	}
}

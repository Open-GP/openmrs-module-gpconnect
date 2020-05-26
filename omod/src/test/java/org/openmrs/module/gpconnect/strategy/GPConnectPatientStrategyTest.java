package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GPConnectPatientStrategyTest {
	
	public static final String TEST_UUID = "test";
	
	@Mock
	NhsPatientMapper nhsPatientMapper;
	
	@Mock
	PatientService mockPatientService;
	
	@InjectMocks
	GPConnectPatientStrategy patientStrategy;

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		ServiceContext serviceContext = ServiceContext.getInstance();
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
	}
	
	@Test
	public void shouldEnhancePatientOnGet() {
		when(mockPatientService.getPatientByUuid(TEST_UUID)).thenReturn(new org.openmrs.Patient(1));
		patientStrategy.getPatient(TEST_UUID);
		verify(nhsPatientMapper).enhance(any());
	}
	
	@Test
	public void shouldEnhancePatientOnSearchById() {
		when(mockPatientService.getPatientByUuid(TEST_UUID)).thenReturn(new org.openmrs.Patient(1));

		Patient enhancedPatient = new Patient();
		when(nhsPatientMapper.enhance(any())).thenReturn(enhancedPatient);
		
		List<Patient> actualPatients = patientStrategy.searchPatientsById(TEST_UUID);
		
		assertEquals(actualPatients.size(), 1);
		assertEquals(actualPatients.get(0), enhancedPatient);
	}

	@Test
	public void shouldEnhancePatientOnSearchByIdentifier() {
		List<PatientIdentifierType> patientIdentifierTypes = Arrays.asList(new PatientIdentifierType(), new PatientIdentifierType());
		List<org.openmrs.Patient> patients = Arrays.asList(new org.openmrs.Patient(1), new org.openmrs.Patient(2));

		when(mockPatientService.getAllPatientIdentifierTypes()).thenReturn(patientIdentifierTypes);
		when(mockPatientService.getPatients("some identifier", null, patientIdentifierTypes, true)).thenReturn(patients);

		Patient enhancedPatient = new Patient();
		Patient secondEnhancedPatient = new Patient();
		when(nhsPatientMapper.enhance(any())).thenReturn(enhancedPatient, secondEnhancedPatient);

		List<Patient> actualPatients = patientStrategy.searchPatientsByIdentifier("some identifier");

		assertEquals(actualPatients.size(), 2);
		assertEquals(actualPatients.get(0), enhancedPatient);
		assertEquals(actualPatients.get(1), secondEnhancedPatient);
	}

	@Test
	public void shouldEnhancePatientOnSearchByFullIdentifier() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		List<PatientIdentifierType> patientIdentifierTypes = Collections.singletonList(patientIdentifierType);
		List<org.openmrs.Patient> patients = Arrays.asList(new org.openmrs.Patient(11));

		when(mockPatientService.getPatientIdentifierTypeByName("identifier type")).thenReturn(patientIdentifierType);
		when(mockPatientService.getPatients("some identifier", null, patientIdentifierTypes, true)).thenReturn(patients);

		Patient enhancedPatient = new Patient();
		when(nhsPatientMapper.enhance(any())).thenReturn(enhancedPatient);

		List<Patient> actualPatients = patientStrategy.searchPatientsByIdentifier("some identifier", "identifier type");

		assertEquals(actualPatients.size(), 1);
		assertEquals(actualPatients.get(0), enhancedPatient);
	}
}

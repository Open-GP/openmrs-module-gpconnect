package org.openmrs.module.gpconnect.strategy;

import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.NhsPatientService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GPConnectPatientStrategyTest {
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();
	
	public static final String TEST_UUID = "test";
	
	@Mock
	NhsPatientMapper mockNhsPatientMapper;
	
	@Mock
	NhsPatientService mockNhsPatientService;
	
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
		verify(mockNhsPatientMapper).enhance(any());
	}
	
	@Test
	public void shouldThrowNotFoundExceptionWhenNoPatientFound() {
		exceptionRule.expect(ResourceNotFoundException.class);
		exceptionRule.expectMessage("No patient details found for patient ID: Patient/test");
		when(mockPatientService.getPatientByUuid(TEST_UUID)).thenReturn(null);
		patientStrategy.getPatient(TEST_UUID);
	}
	
	@Test
	public void shouldThrowInvalidExceptionWhenNoPatientIdentifiersFound() {
		exceptionRule.expect(InvalidRequestException.class);
		exceptionRule.expectMessage("The given identifier system code (something) is not an expected code");
		when(mockPatientService.getPatientIdentifierTypeByName("something")).thenReturn(null);
		patientStrategy.searchPatientsByIdentifier("value", "something");
	}
	
	@Test
	public void shouldThrowInvalidParameterForWhenSystemIsMissing() {
		exceptionRule.expect(UnprocessableEntityException.class);
		exceptionRule
		        .expectMessage("One or both of the identifier system and value are missing from given identifier : something");
		patientStrategy.searchPatientsByIdentifier("something");
	}
	
	@Test
	public void shouldEnhancePatientOnSearchById() {
		when(mockPatientService.getPatientByUuid(TEST_UUID)).thenReturn(new org.openmrs.Patient(1));
		
		Patient enhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient);
		
		List<Patient> actualPatients = patientStrategy.searchPatientsById(TEST_UUID);
		
		assertEquals(actualPatients.size(), 1);
		assertEquals(actualPatients.get(0), enhancedPatient);
	}
	
	@Test
	public void shouldEnhancePatientOnSearchByFullIdentifier() {
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		List<PatientIdentifierType> patientIdentifierTypes = Collections.singletonList(patientIdentifierType);
		List<org.openmrs.Patient> patients = Arrays.asList(new org.openmrs.Patient(11));
		
		when(mockPatientService.getPatientIdentifierTypeByName("identifier type")).thenReturn(patientIdentifierType);
		when(mockPatientService.getPatients("some identifier", null, patientIdentifierTypes, true)).thenReturn(patients);
		
		Patient enhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient);
		
		List<Patient> actualPatients = patientStrategy.searchPatientsByIdentifier("some identifier", "identifier type");
		
		assertEquals(actualPatients.size(), 1);
		assertEquals(actualPatients.get(0), enhancedPatient);
	}
	
	@Test
	public void shouldEnhancePatientOnSearch() {
		List<org.openmrs.Patient> patients = Arrays.asList(new org.openmrs.Patient(1), new org.openmrs.Patient(2),
		    new org.openmrs.Patient(3));
		
		when(mockPatientService.getAllPatients(true)).thenReturn(patients);
		
		Patient enhancedPatient = new Patient();
		Patient secondEnhancedPatient = new Patient();
		Patient thirdEnhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient, secondEnhancedPatient, thirdEnhancedPatient);
		
		List<Patient> actualPatients = patientStrategy.searchPatients(true);
		
		assertEquals(actualPatients.size(), 3);
		assertEquals(actualPatients.get(0), enhancedPatient);
		assertEquals(actualPatients.get(1), secondEnhancedPatient);
		assertEquals(actualPatients.get(2), thirdEnhancedPatient);
	}
	
	@Test
	public void shouldEnhancePatientOnSearchByName() {
		org.openmrs.Patient patient = new org.openmrs.Patient(1);
		HashSet<PersonName> names = new HashSet<>(Collections.singletonList(
				new PersonName("joe", "m", "doe")
		));
		patient.setNames(names);

		org.openmrs.Patient secondPatient = new org.openmrs.Patient(2);
		HashSet<PersonName> secondNames = new HashSet<>(Collections.singletonList(
				new PersonName("joe", "m", "jones")
		));
		secondPatient.setNames(secondNames);

		when(mockPatientService.getPatients("joe")).thenReturn(Arrays.asList(patient, secondPatient));

		Patient enhancedPatient = new Patient();
		Patient secondEnhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient, secondEnhancedPatient);

		Bundle patientsBundle = patientStrategy.searchPatientsByGivenName("joe");

		List<Bundle.BundleEntryComponent> patientsBundleEntry = patientsBundle.getEntry();

		assertEquals(patientsBundleEntry.size(), 2);
		assertEquals(patientsBundleEntry.get(0).getResource(), enhancedPatient);
		assertEquals(patientsBundleEntry.get(1).getResource(), secondEnhancedPatient);
	}
	
	@Test
	public void shouldEnhancePatientOnSearchByFamilyName() {
		org.openmrs.Patient patient = new org.openmrs.Patient(1);
		HashSet<PersonName> names = new HashSet<>(Collections.singletonList(
				new PersonName("tom", "m", "jones")
		));
		patient.setNames(names);

		org.openmrs.Patient secondPatient = new org.openmrs.Patient(2);
		HashSet<PersonName> secondNames = new HashSet<>(Collections.singletonList(
				new PersonName("joe", "m", "jones")
		));
		secondPatient.setNames(secondNames);

		when(mockPatientService.getPatients("jones")).thenReturn(Arrays.asList(patient, secondPatient));

		Patient enhancedPatient = new Patient();
		Patient secondEnhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient, secondEnhancedPatient);

		Bundle patientsBundle = patientStrategy.searchPatientsByFamilyName("jones");

		List<Bundle.BundleEntryComponent> patientsBundleEntry = patientsBundle.getEntry();

		assertEquals(patientsBundleEntry.size(), 2);
		assertEquals(patientsBundleEntry.get(0).getResource(), enhancedPatient);
		assertEquals(patientsBundleEntry.get(1).getResource(), secondEnhancedPatient);
	}
	
	@Test
	public void shouldEnhancePatientOnSearchByAnyName() {
		org.openmrs.Patient patient = new org.openmrs.Patient(1);
		HashSet<PersonName> names = new HashSet<>(Collections.singletonList(
				new PersonName("jones", "m", "tom")
		));
		patient.setNames(names);

		org.openmrs.Patient secondPatient = new org.openmrs.Patient(2);
		HashSet<PersonName> secondNames = new HashSet<>(Collections.singletonList(
				new PersonName("joe", "m", "jones")
		));
		secondPatient.setNames(secondNames);

		when(mockPatientService.getPatients("jones")).thenReturn(Arrays.asList(patient, secondPatient));

		Patient enhancedPatient = new Patient();
		Patient secondEnhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient, secondEnhancedPatient);

		Bundle patientsBundle = patientStrategy.searchPatientsByName("jones");

		List<Bundle.BundleEntryComponent> patientsBundleEntry = patientsBundle.getEntry();

		assertEquals(patientsBundleEntry.size(), 2);
		assertEquals(patientsBundleEntry.get(0).getResource(), enhancedPatient);
		assertEquals(patientsBundleEntry.get(1).getResource(), secondEnhancedPatient);
	}
	
	@Test
	public void shouldSaveNhsRelatedData() {
		Patient patient = createPatient();
		
		NhsPatient nhsPatient = new NhsPatient();
		
		long patientId = 1L;
		org.openmrs.Patient omrsPatient = new org.openmrs.Patient((int) patientId);
		
		when(mockPatientService.savePatient(any())).thenReturn(omrsPatient);
		when(mockPatientService.getPatientByUuid(any())).thenReturn(omrsPatient);
		when(mockNhsPatientMapper.toNhsPatient(patient, patientId)).thenReturn(nhsPatient);
		Patient enhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient);
		
		Patient actualPatient = patientStrategy.createFHIRPatient(patient);
		
		verify(mockNhsPatientService).saveOrUpdate(nhsPatient);
		assertEquals(enhancedPatient, actualPatient);
	}
	
	@Test
	public void shouldUpdateTheNhsPatientData() {
		Patient patient = createPatient();
		NhsPatient nhsPatient = new NhsPatient();
		
		when(mockPatientService.getPatientByUuid(any())).thenReturn(new org.openmrs.Patient(1));
		long patientId = 1L;
		org.openmrs.Patient omrsPatient = new org.openmrs.Patient((int) patientId);
		
		when(mockPatientService.savePatient(any())).thenReturn(omrsPatient);
		when(mockPatientService.getPatientByUuid(any())).thenReturn(omrsPatient);
		when(mockNhsPatientMapper.toNhsPatient(patient, patientId)).thenReturn(nhsPatient);
		Patient enhancedPatient = new Patient();
		when(mockNhsPatientMapper.enhance(any())).thenReturn(enhancedPatient);
		
		Patient actualPatient = patientStrategy.updatePatient(patient, "test");
		verify(mockNhsPatientService).saveOrUpdate(nhsPatient);
		assertEquals(enhancedPatient, actualPatient);
	}
	
	private Patient createPatient() {
		Patient patient = new Patient();
		patient.setDeceased(new BooleanType(false));
		Identifier identifier = new Identifier();
		identifier.setUse(Identifier.IdentifierUse.USUAL);
		identifier.setSystem("system");
		PatientIdentifierType patientIdentifierType = new PatientIdentifierType();
		patientIdentifierType.setLocationBehavior(PatientIdentifierType.LocationBehavior.NOT_USED);
		when(mockPatientService.getPatientIdentifierTypeByName("system")).thenReturn(patientIdentifierType);
		
		patient.setIdentifier(Collections.singletonList(identifier));
		HumanName humanName = new HumanName();
		humanName.setFamily("doe");
		humanName.setGiven(Collections.singletonList(new StringType("joe")));
		patient.setName(Arrays.asList(humanName));
		
		patient.setGender(Enumerations.AdministrativeGender.FEMALE);
		return patient;
	}
}

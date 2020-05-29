package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.openmrs.module.gpconnect.util.Extensions.NHS_VERFICATION_STATUS_URL;

public class NhsPatientMapperTest {
	
	@Mock
	NhsPatientService mockNhsPatientService;
	
	@Mock
	PatientService mockPatientService;
	
	@InjectMocks
	NhsPatientMapper nhsPatientMapper;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	private void setup(String patientUuid) {
		patient.setId(patientUuid);
		ServiceContext serviceContext = ServiceContext.getInstance();
		when(mockPatientService.getPatientByUuid(patientUuid)).thenReturn(new org.openmrs.Patient(1));
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
	}
	
	Patient patient = new Patient();
	
	@Test
	public void shouldMapFakeEmail() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		when(mockNhsPatientService.findById(any())).thenReturn(null);
		
		Patient actualPatient = nhsPatientMapper.enhance(patient);
		
		assertEquals(actualPatient.getTelecom().get(0).getValue(), "test@mail.com");
	}
	
	@Test
	public void shouldSetTheCadavericDonorExtension() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.cadavericDonor = true;
		
		when(mockNhsPatientService.findById(any())).thenReturn(nhsPatient);
		
		Patient actualPatient = nhsPatientMapper.enhance(patient);
		
		Extension extension = actualPatient.getExtensionsByUrl(Extensions.CADAVERIC_DONOR_URL).get(0);
		assertEquals(((BooleanType) extension.getValue()).booleanValue(), true);
	}
	
	@Test
	public void shouldMapNhsPatient() {
		Patient patient = new Patient();
		
		patient.setExtension(Collections.singletonList(new Extension(Extensions.CADAVERIC_DONOR_URL, new BooleanType(true))));
		
		NhsPatient expectedPatient = new NhsPatient();
		expectedPatient.setCadavericDonor(true);
		expectedPatient.setId(3L);
		
		assertEquals(expectedPatient, nhsPatientMapper.toNhsPatient(patient, 3));
	}
	
	@Test
	public void shouldSkipCadavericDonorWhenMissing() {
		Patient patient = new Patient();
		
		NhsPatient expectedPatient = new NhsPatient();
		expectedPatient.setId(3L);
		
		assertEquals(expectedPatient, nhsPatientMapper.toNhsPatient(patient, 3));
	}
	
	@Test
	public void shouldAddNhsNumber() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.nhsNumber = "123456";
		
		when(mockNhsPatientService.findById(any())).thenReturn(nhsPatient);
		
		Patient actualPatient = nhsPatientMapper.enhance(patient);
		
		Identifier identifier = actualPatient.getIdentifier().get(0);
		
		assertEquals(identifier.getSystem(), "https://fhir.nhs.uk/Id/nhs-number");
		assertEquals(identifier.getValue(), "123456");
	}
	
	@Test
	public void shouldAddNhsNumberVerificationStatus() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.nhsNumber = "123456";
		nhsPatient.nhsNumberVerificationStatus = "01";
		
		when(mockNhsPatientService.findById(any())).thenReturn(nhsPatient);
		
		Patient actualPatient = nhsPatientMapper.enhance(patient);
		
		Identifier identifier = actualPatient.getIdentifier().get(0);
		
		Extension extension = identifier.getExtensionsByUrl(NHS_VERFICATION_STATUS_URL).get(0);
		assertEquals("01", ((StringType) extension.getValue()).getValue());
	}
	
	@Test
	public void shouldMapNhsNumber() {
		Patient patient = new Patient();
		
		Identifier nhsNoIdentifier = new Identifier();
		nhsNoIdentifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
		nhsNoIdentifier.setValue("123");
		Extension verificationStatus = new Extension(NHS_VERFICATION_STATUS_URL, new StringType("02"));
		nhsNoIdentifier.setExtension(Collections.singletonList(verificationStatus));
		
		patient.addIdentifier(nhsNoIdentifier);
		
		NhsPatient expectedPatient = new NhsPatient();
		expectedPatient.setId(3L);
		expectedPatient.setNhsNumber("123");
		expectedPatient.setNhsNumberVerificationStatus("02");
		
		assertEquals(expectedPatient, nhsPatientMapper.toNhsPatient(patient, 3));
	}
	
	@Test
	public void shouldSkipMappingNhsNumberWhenMissing() {
		Patient patient = new Patient();
		
		NhsPatient expectedPatient = new NhsPatient();
		expectedPatient.setId(3L);
		
		assertEquals(expectedPatient, nhsPatientMapper.toNhsPatient(patient, 3));
	}
}

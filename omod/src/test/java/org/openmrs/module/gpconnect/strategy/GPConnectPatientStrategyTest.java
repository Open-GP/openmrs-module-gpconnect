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
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class GPConnectPatientStrategyTest {
	
	@Mock
	NhsPatientService mockNhsPatientService;
	
	@Mock
	PatientService mockPatientService;
	
	@InjectMocks
	GPConnectPatientStrategy patientStrategy;
	
	@Before
	public void initMocks() {
		MockitoAnnotations.initMocks(this);
	}
	
	private void setup(String patientUuid) {
		ServiceContext serviceContext = ServiceContext.getInstance();
		when(mockPatientService.getPatientByUuid(patientUuid)).thenReturn(new org.openmrs.Patient(1));
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
	}
	
	@Test
	public void shouldMapFakeEmail() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		when(mockNhsPatientService.findById(any())).thenReturn(null);
		
		Patient actualPatient = patientStrategy.getPatient(patientUuid);
		
		assertEquals(actualPatient.getTelecom().get(0).getValue(), "test@mail.com");
	}
	
	@Test
	public void shouldSetTheCadavericDonorExtension() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.cadavericDonor = true;
		
		when(mockNhsPatientService.findById(any())).thenReturn(nhsPatient);
		
		Patient patient = patientStrategy.getPatient(patientUuid);
		
		Extension extension = patient.getExtensionsByUrl(GPConnectExtensions.CADAVERIC_DONOR_URL).get(0);
		assertEquals(((BooleanType) extension.getValue()).booleanValue(), true);
	}
}

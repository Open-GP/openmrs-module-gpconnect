package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GPConnectPatientStrategyTest {
	
	@Test
	public void shouldMapFakeEmail() {
		String patientUuid = "test";
		
		ServiceContext serviceContext = ServiceContext.getInstance();
		PatientService mockPatientService = mock(PatientService.class);
		when(mockPatientService.getPatientByUuid(patientUuid)).thenReturn(new org.openmrs.Patient());
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
		GPConnectPatientStrategy patientStrategy = new GPConnectPatientStrategy();
		
		Patient test = patientStrategy.getPatient(patientUuid);
		
		assertEquals(test.getTelecom().get(0).getValue(), "test@mail.com");
	}
}

package org.openmrs.module.gpconnect.strategy;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.ServiceContext;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GPConnectPatientStrategyTest {
	
	@Mock
	NhsPatientMapper nhsPatientMapper;

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
	public void shouldEnhancePatientOnGet() {
		setup("test");
		patientStrategy.getPatient("test");
		verify(nhsPatientMapper).enhance(any(),eq("test"));
	}
}

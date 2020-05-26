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
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;

import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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
		setup(TEST_UUID);
		patientStrategy.getPatient(TEST_UUID);
		verify(nhsPatientMapper).enhance(any(),eq(TEST_UUID));
	}

	@Test
	public void shouldEnhancePatientOnSearch() {
		setup(TEST_UUID);
		Patient enhancedPatient = new Patient();
		when(nhsPatientMapper.enhance(any(), eq(TEST_UUID))).thenReturn(enhancedPatient);

		List<Patient> actualPatients = patientStrategy.searchPatientsById(TEST_UUID);

		assertEquals(actualPatients.size(), 1);
		assertEquals(actualPatients.get(0), enhancedPatient);
	}
}

package org.openmrs.module.gpconnect.mappers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Identifier;
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
import org.openmrs.module.gpconnect.util.Extensions;

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
		patient.addIdentifier(new Identifier().setSystem(Extensions.NHS_NUMBER_SYSTEM).setValue("123"));
		ServiceContext serviceContext = ServiceContext.getInstance();
		when(mockPatientService.getPatientByUuid(patientUuid)).thenReturn(new org.openmrs.Patient(1));
		serviceContext.setPatientService(mockPatientService);
		Context.setContext(serviceContext);
	}
	
	Patient patient = new Patient();
	
	@Test
	public void shouldMapNhsPatient() {
		Patient patient = new Patient();
		
		NhsPatient expectedPatient = new NhsPatient();
		expectedPatient.setId(3L);
		
		assertEquals(expectedPatient, nhsPatientMapper.toNhsPatient(patient, 3));
	}
	
	@Test
	public void shouldEnhanceWithMetada() {
		String patientUuid = "test";
		
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		
		when(mockNhsPatientService.findById(any())).thenReturn(nhsPatient);
		
		Patient actualPatient = nhsPatientMapper.enhance(patient);
		
		assertEquals("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Patient-1", actualPatient.getMeta()
		        .getProfile().get(0).asStringValue());
		
		assertEquals("test-1", actualPatient.getMeta().getVersionId());
		
	}
	
	@Test
	public void shouldHandleTheDeceasedField() {
		String patientUuid = "test";
		
		patient.setDeceased(new BooleanType(false));
		setup(patientUuid);
		
		NhsPatient nhsPatient = new NhsPatient();
		
		when(mockNhsPatientService.findById(any())).thenReturn(nhsPatient);
		
		Patient actualPatient = nhsPatientMapper.enhance(patient);
		
		assertNull(actualPatient.getDeceased());
	}
}

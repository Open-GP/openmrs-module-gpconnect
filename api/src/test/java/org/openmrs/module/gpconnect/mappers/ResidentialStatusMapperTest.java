package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResidentialStatusMapperTest {
	
	public static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
	
	CodeableConceptExtension mockCodeableConcept = mock(CodeableConceptExtension.class);
	
	ResidentialStatusMapper residentialStatueMapper = new ResidentialStatusMapper(mockCodeableConcept);
	
	Patient patient = new Patient();
	
	@Test
	public void shouldSetResidentialStatus() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.residentialStatus = "H";
		
		Extension extension = new Extension(Extensions.RESIDENTIAL_STATUS_URL);
		when(mockCodeableConcept.createExtension("H")).thenReturn(Optional.of(extension));
		
		Patient actualPatient = residentialStatueMapper.enhance(patient, nhsPatient);
		
		Extension actualExtension = actualPatient.getExtensionsByUrl(Extensions.RESIDENTIAL_STATUS_URL).get(0);
		
		assertEquals(extension, actualExtension);
	}
	
	@Test
	public void shouldNotSetResidentialStatusWhenUnknown() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.residentialStatus = "something";
		
		when(mockCodeableConcept.createExtension("something")).thenReturn(Optional.empty());
		
		Patient actualPatient = residentialStatueMapper.enhance(patient, nhsPatient);
		
		assertEquals(0, actualPatient.getExtensionsByUrl(Extensions.RESIDENTIAL_STATUS_URL).size());
	}
	
	@Test
	public void shouldMapResidentialStatus() {
		Patient patient = new Patient();
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.setResidentialStatus("H");
		
		when(mockCodeableConcept.getValue(patient)).thenReturn(Optional.of("H"));
		
		NhsPatient actualPatient = residentialStatueMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);
		
		assertEquals(nhsPatient, actualPatient);
	}
	
	@Test
	public void shouldSkipResidentialStatusMappingWhenSystemUnknown() {
		Patient patient = new Patient();
		when(mockCodeableConcept.getValue(patient)).thenReturn(Optional.empty());
		
		NhsPatient actualPatient = residentialStatueMapper.mapToNhsPatient(patient, EMPTY_NHS_PATIENT);
		
		assertEquals(EMPTY_NHS_PATIENT, actualPatient);
	}
}

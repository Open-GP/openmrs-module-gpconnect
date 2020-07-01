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

public class EthnicCategoryMapperTest {
	
	public static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
	
	CodeableConceptExtension mockCodeableConcept = mock(CodeableConceptExtension.class);
	
	EthnicCategoryMapper ethnicCategoryMapper = new EthnicCategoryMapper(mockCodeableConcept);
	
	Patient patient = new Patient();
	
	@Test
	public void shouldSetEthnicCategory() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.ethnicCategory = "C";
		
		Extension extension = new Extension(Extensions.ETHNIC_CATEGORY_URL);
		when(mockCodeableConcept.createExtension("C")).thenReturn(Optional.of(extension));
		
		Patient actualPatient = ethnicCategoryMapper.enhance(patient, nhsPatient);
		
		Extension actualExtension = actualPatient.getExtensionsByUrl(Extensions.ETHNIC_CATEGORY_URL).get(0);
		
		assertEquals(extension, actualExtension);
	}
	
	@Test
	public void shouldNotSetEthnicCategoryWhenUnknown() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.ethnicCategory = "something";
		
		when(mockCodeableConcept.createExtension("something")).thenReturn(Optional.empty());
		
		Patient actualPatient = ethnicCategoryMapper.enhance(patient, nhsPatient);
		
		assertEquals(0, actualPatient.getExtensionsByUrl(Extensions.ETHNIC_CATEGORY_URL).size());
	}
	
	@Test
	public void shouldMapEthnicCategory() {
		Patient patient = new Patient();
		
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.setEthnicCategory("CT");
		
		when(mockCodeableConcept.getValue(patient)).thenReturn(Optional.of("CT"));
		
		NhsPatient actualPatient = ethnicCategoryMapper.mapToNhsPatient(patient, new NhsPatient());
		
		assertEquals(nhsPatient, actualPatient);
	}
	
	@Test
	public void shouldSkipEthnicCategoryMappingWhenSystemUnknown() {
		Patient patient = new Patient();
		
		when(mockCodeableConcept.getValue(patient)).thenReturn(Optional.empty());
		
		NhsPatient actualPatient = ethnicCategoryMapper.mapToNhsPatient(patient, new NhsPatient());
		
		assertEquals(EMPTY_NHS_PATIENT, actualPatient);
	}
	
}

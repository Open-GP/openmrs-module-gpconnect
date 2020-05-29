package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.openmrs.module.gpconnect.util.Extensions.NHS_VERFICATION_STATUS_URL;

public class NhsNoMapperTest {
	
	private static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
	
	NhsNoMapper mapper = new NhsNoMapper();
	
	Patient patient = new Patient();
	
	@Test
	public void shouldAddNhsNumber() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.nhsNumber = "123456";
		
		Patient actualPatient = mapper.enhance(patient, nhsPatient);
		
		Identifier identifier = actualPatient.getIdentifier().get(0);
		
		assertEquals(identifier.getSystem(), "https://fhir.nhs.uk/Id/nhs-number");
		assertEquals(identifier.getValue(), "123456");
	}
	
	@Test
	public void shouldAddNhsNumberVerificationStatus() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.nhsNumber = "123456";
		nhsPatient.nhsNumberVerificationStatus = "01";
		
		Patient actualPatient = mapper.enhance(patient, nhsPatient);
		
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
		expectedPatient.setNhsNumber("123");
		expectedPatient.setNhsNumberVerificationStatus("02");
		
		assertEquals(expectedPatient, mapper.mapToNhsPatient(patient, new NhsPatient()));
	}
	
	@Test
	public void shouldSkipMappingNhsNumberWhenMissing() {
		Patient patient = new Patient();
		
		assertEquals(EMPTY_NHS_PATIENT, mapper.mapToNhsPatient(patient, new NhsPatient()));
	}
	
}

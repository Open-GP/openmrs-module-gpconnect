package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openmrs.module.gpconnect.util.Extensions.NHS_VERFICATION_STATUS_URL;

public class NhsNoMapperTest {
	
	private static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
	
	CodeableConceptExtension nhsNoVerificationStatus = mock(CodeableConceptExtension.class);
	
	NhsNoMapper mapper = new NhsNoMapper(nhsNoVerificationStatus);
	
	Patient patient = new Patient();
	
	@Before
	public void setUp() throws Exception {
		when(nhsNoVerificationStatus.createExtension(any())).thenReturn(Optional.empty());
		when(nhsNoVerificationStatus.getValue(any(Identifier.class))).thenReturn(Optional.empty());

		Identifier simpleNhsNo = new Identifier();
		simpleNhsNo.setSystem(Extensions.NHS_NUMBER_SYSTEM);
		simpleNhsNo.setValue("123456");
		simpleNhsNo.setUse(Identifier.IdentifierUse.USUAL);
		simpleNhsNo.setPeriod(new Period());
		simpleNhsNo.setAssigner(new Reference());
		patient.addIdentifier(simpleNhsNo);
	}
	
	@Test
	public void shouldReplacePreviousNhsNoIdentifier() {
		NhsPatient nhsPatient = new NhsPatient();

		Patient actualPatient = mapper.enhance(patient, nhsPatient);

		assertEquals(1, actualPatient.getIdentifier().size());
		Identifier identifier = actualPatient.getIdentifier().get(0);
		assertEquals(Extensions.NHS_NUMBER_SYSTEM, identifier.getSystem());
		assertEquals("123456", identifier.getValue());
		assertNull(identifier.getUse());
	}
	
	@Test
	public void shouldAddNhsNumberVerificationStatus() {
		NhsPatient nhsPatient = new NhsPatient();
		nhsPatient.nhsNumberVerificationStatus = "01";
		
		Extension verificationStatusExt = new Extension(NHS_VERFICATION_STATUS_URL);
		when(nhsNoVerificationStatus.createExtension("01")).thenReturn(Optional.of(verificationStatusExt));
		Patient actualPatient = mapper.enhance(patient, nhsPatient);
		
		Identifier identifier = actualPatient.getIdentifier().get(0);
		
		Extension extension = identifier.getExtensionsByUrl(NHS_VERFICATION_STATUS_URL).get(0);
		assertEquals(verificationStatusExt, extension);
	}
	
	@Test
	public void shouldMapNhsNumber() {
		Patient patient = new Patient();
		
		Identifier nhsNoIdentifier = new Identifier();
		nhsNoIdentifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
		nhsNoIdentifier.setValue("123");
		Extension verificationStatus = new Extension(NHS_VERFICATION_STATUS_URL);
		nhsNoIdentifier.setExtension(Collections.singletonList(verificationStatus));
		
		when(nhsNoVerificationStatus.getValue(nhsNoIdentifier)).thenReturn(Optional.of("02"));
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

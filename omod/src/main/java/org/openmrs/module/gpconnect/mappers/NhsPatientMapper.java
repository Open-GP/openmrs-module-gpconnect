package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.openmrs.api.PatientService;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class NhsPatientMapper {
	
	@Autowired
	NhsPatientService nhsPatientService;
	
	@Autowired
	PatientService patientService;
	
	public Patient enhance(Patient patient) {
		if (patient == null) {
			return null;
		}
		
		String uuid = patient.getId();
		
		ContactPoint email = new ContactPoint();
		email.setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("test@mail.com");
		patient.addTelecom(email);
		
		org.openmrs.Patient omrsPatient = patientService.getPatientByUuid(uuid);
		
		if (omrsPatient == null) {
			return patient;
		}
		
		NhsPatient nhsPatient = nhsPatientService.findById(omrsPatient.getPatientId().longValue());
		
		if (nhsPatient == null) {
			return patient;
		}
		
		Extension cadavericDonor = new Extension(GPConnectExtensions.CADAVERIC_DONOR_URL, new BooleanType(
		        nhsPatient.cadavericDonor));
		patient.addExtension(cadavericDonor);
		
		Identifier nhsNoIdentifier = new Identifier();
		nhsNoIdentifier.setSystem("https://fhir.nhs.uk/Id/nhs-number");
		nhsNoIdentifier.setValue(nhsPatient.nhsNumber);
		Extension verficationStatus = new Extension(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/Extension-CareConnect-GPC-NHSNumberVerificationStatus-1",
		        new StringType(nhsPatient.nhsNumberVerificationStatus));
		nhsNoIdentifier.setExtension(Collections.singletonList(verficationStatus));
		
		patient.addIdentifier(nhsNoIdentifier);
		
		return patient;
	}
	
	public NhsPatient toNhsPatient(Patient patient, long patientId) {
		NhsPatient nhsPatient = new NhsPatient();
		List<Extension> extensionsByUrl = patient.getExtensionsByUrl(GPConnectExtensions.CADAVERIC_DONOR_URL);
		if (extensionsByUrl.size() > 0) {
			nhsPatient.cadavericDonor = ((BooleanType) extensionsByUrl.get(0).getValue()).booleanValue();
		}
		nhsPatient.setId(patientId);
		return nhsPatient;
	}
}

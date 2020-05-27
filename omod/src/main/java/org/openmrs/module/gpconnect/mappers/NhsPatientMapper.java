package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.openmrs.api.PatientService;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.EthnicCategory;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
		nhsNoIdentifier.setSystem(GPConnectExtensions.NHS_NUMBER_SYSTEM);
		nhsNoIdentifier.setValue(nhsPatient.nhsNumber);
		Extension verficationStatus = new Extension(GPConnectExtensions.NHS_VERFICATION_STATUS_URL, new StringType(
		        nhsPatient.nhsNumberVerificationStatus));
		nhsNoIdentifier.setExtension(Collections.singletonList(verficationStatus));
		
		patient.addIdentifier(nhsNoIdentifier);
		
		if (nhsPatient.ethnicCategory != null) {
			try {
				EthnicCategory ethnicCategoryEnum = EthnicCategory.valueOf(nhsPatient.ethnicCategory);
				CodeableConcept ethnicConcept = new CodeableConcept();
				ethnicConcept.addCoding(ethnicCategoryEnum.getCoding());
				Extension ethnicCategory = new Extension(GPConnectExtensions.ETHNIC_CATEGORY_URL, ethnicConcept);
				patient.addExtension(ethnicCategory);
			}
			catch (IllegalArgumentException e) {
				System.out.printf("The ethnic category: %s is not a known one\n", nhsPatient.ethnicCategory);
			}
		}
		
		return patient;
	}
	
	public NhsPatient toNhsPatient(Patient patient, long patientId) {
		NhsPatient nhsPatient = new NhsPatient();
		List<Extension> extensionsByUrl = patient.getExtensionsByUrl(GPConnectExtensions.CADAVERIC_DONOR_URL);
		if (extensionsByUrl.size() > 0) {
			nhsPatient.cadavericDonor = ((BooleanType) extensionsByUrl.get(0).getValue()).booleanValue();
		}

		Optional<Identifier> optionalNhsNo = patient.getIdentifier()
				.stream()
				.filter((identifier -> identifier.getSystem().equals(GPConnectExtensions.NHS_NUMBER_SYSTEM)))
				.findFirst();

		if (optionalNhsNo.isPresent()) {
			nhsPatient.setNhsNumber(optionalNhsNo.get().getValue());
			nhsPatient.setNhsNumberVerificationStatus(
					optionalNhsNo.get()
							.getExtensionString(GPConnectExtensions.NHS_VERFICATION_STATUS_URL));

		}

		List<Extension> ethnicCategoryExtensions = patient.getExtensionsByUrl(GPConnectExtensions.ETHNIC_CATEGORY_URL);
		if (ethnicCategoryExtensions.size() > 0) {
			Coding coding = ((CodeableConcept) ethnicCategoryExtensions.get(0).getValue()).getCoding().get(0);

			if (coding.getSystem().equals(CodeSystems.ETHNIC_CATEGORY) && EthnicCategory.isValid(coding.getCode())) {
				nhsPatient.setEthnicCategory(coding.getCode());
			}

		}

		nhsPatient.setId(patientId);

		return nhsPatient;
	}
}

package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.openmrs.api.PatientService;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.DeathNotificationStatus;
import org.openmrs.module.gpconnect.mappers.valueSets.EthnicCategory;
import org.openmrs.module.gpconnect.mappers.valueSets.RegistrationType;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.mappers.valueSets.TreatmentCategory;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class NhsPatientMapper {
	
	@Autowired
	NhsPatientService nhsPatientService;
	
	@Autowired
	PatientService patientService;
	
	List<PatientFieldMapper> mappers;
	
	public NhsPatientMapper() {
		mappers = Arrays.asList(new CadavericDonorMapper(new BooleanExtension(Extensions.CADAVERIC_DONOR_URL)),
		    new EthnicCategoryMapper(new CodeableConceptExtension(Extensions.ETHNIC_CATEGORY_URL,
		            CodeSystems.ETHNIC_CATEGORY, EthnicCategory.dict())), new RegistrationDetailsMapper(
		            new CodeableConceptExtension(Extensions.REGISTRATION_TYPE, CodeSystems.REGISTRATION_TYPE,
		                    RegistrationType.dict())), new TreatmentCategoryMapper(new CodeableConceptExtension(
		            Extensions.TREATMENT_CATEGORY_URL, CodeSystems.TREATMENT_CATEGORY, TreatmentCategory.dict())),
		    new ResidentialStatusMapper(new CodeableConceptExtension(Extensions.RESIDENTIAL_STATUS_URL,
		            CodeSystems.RESIDENTIAL_STATUS, ResidentialStatus.dict())), new DeathNotificationStatusMapper(
		            new CodeableConceptExtension(Extensions.DEATH_NOTIFICATION_STATUS_URL,
		                    CodeSystems.DEATH_NOTIFICATION_STATUS, DeathNotificationStatus.dict())));
	}
	
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

		Identifier nhsNoIdentifier = new Identifier();
		nhsNoIdentifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
		nhsNoIdentifier.setValue(nhsPatient.nhsNumber);
		Extension verficationStatus = new Extension(Extensions.NHS_VERFICATION_STATUS_URL, new StringType(
		        nhsPatient.nhsNumberVerificationStatus));
		nhsNoIdentifier.setExtension(Collections.singletonList(verficationStatus));
		
		patient.addIdentifier(nhsNoIdentifier);

		patient = mappers.stream()
				.reduce(patient,
						(currentPatient, patientFieldMapper) -> patientFieldMapper.enhance(currentPatient, nhsPatient),
						(currentPatient, formerPatient) -> currentPatient
				);

		return patient;
	}
	
	public NhsPatient toNhsPatient(Patient patient, long patientId) {
		NhsPatient nhsPatient = new NhsPatient();

		Optional<Identifier> optionalNhsNo = patient.getIdentifier()
				.stream()
				.filter((identifier -> identifier.getSystem().equals(Extensions.NHS_NUMBER_SYSTEM)))
				.findFirst();

		if (optionalNhsNo.isPresent()) {
			nhsPatient.setNhsNumber(optionalNhsNo.get().getValue());
			nhsPatient.setNhsNumberVerificationStatus(
					optionalNhsNo.get()
							.getExtensionString(Extensions.NHS_VERFICATION_STATUS_URL));

		}

		nhsPatient = mappers.stream()
				.reduce(nhsPatient,
						(currentNhsPatient, patientFieldMapper) -> patientFieldMapper.mapToNhsPatient(patient, currentNhsPatient),
						(currentPatient, formerPatient) -> currentPatient
				);

		nhsPatient.setId(patientId);

		return nhsPatient;
	}
}

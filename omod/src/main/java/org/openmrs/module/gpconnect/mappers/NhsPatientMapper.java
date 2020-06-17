package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.DeathNotificationStatus;
import org.openmrs.module.gpconnect.mappers.valueSets.EthnicCategory;
import org.openmrs.module.gpconnect.mappers.valueSets.NhsNoVerificationStatus;
import org.openmrs.module.gpconnect.mappers.valueSets.RegistrationType;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.mappers.valueSets.TreatmentCategory;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class NhsPatientMapper {
	
	@Autowired
	NhsPatientService nhsPatientService;
	
	@Autowired
	PatientService patientService;
	
	List<PatientFieldMapper> mappers;
	
	public NhsPatientMapper() {
		CadavericDonorMapper cadavericDonor = new CadavericDonorMapper(new BooleanExtension(Extensions.CADAVERIC_DONOR_URL));
		EthnicCategoryMapper ethnicCategory = new EthnicCategoryMapper(new CodeableConceptExtension(
		        Extensions.ETHNIC_CATEGORY_URL, CodeSystems.ETHNIC_CATEGORY, EthnicCategory.dict()));
		RegistrationDetailsMapper registrationDetails = new RegistrationDetailsMapper(new CodeableConceptExtension(
		        Extensions.REGISTRATION_TYPE, CodeSystems.REGISTRATION_TYPE, RegistrationType.dict()));
		TreatmentCategoryMapper treatmentCategory = new TreatmentCategoryMapper(new CodeableConceptExtension(
		        Extensions.TREATMENT_CATEGORY_URL, CodeSystems.TREATMENT_CATEGORY, TreatmentCategory.dict()));
		ResidentialStatusMapper residentialStatus = new ResidentialStatusMapper(new CodeableConceptExtension(
		        Extensions.RESIDENTIAL_STATUS_URL, CodeSystems.RESIDENTIAL_STATUS, ResidentialStatus.dict()));
		DeathNotificationStatusMapper deathNotificationStatus = new DeathNotificationStatusMapper(
		        new CodeableConceptExtension(Extensions.DEATH_NOTIFICATION_STATUS_URL,
		                CodeSystems.DEATH_NOTIFICATION_STATUS, DeathNotificationStatus.dict()));
		
		NhsNoMapper nhsNo = new NhsNoMapper(new CodeableConceptExtension(Extensions.NHS_VERFICATION_STATUS_URL,
		        CodeSystems.NHS_NO_VERIFICATION_STATUS, NhsNoVerificationStatus.dict()));
		mappers = Arrays.asList(nhsNo, cadavericDonor, ethnicCategory, registrationDetails, treatmentCategory,
		    residentialStatus, deathNotificationStatus);
	}
	
	public Patient enhance(Patient patient) {
		if (patient == null) {
			return null;
		}
		
		String uuid = patient.getId();
		
		org.openmrs.Patient omrsPatient = patientService.getPatientByUuid(uuid);
		
		if (omrsPatient == null) {
			return patient;
		}
		
		NhsPatient nhsPatient = nhsPatientService.findById(omrsPatient.getPatientId().longValue());

		Meta meta = new Meta();
		meta.setVersionId(String.format("%s-1", uuid));
		meta.addProfile("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Patient-1");
		patient.setMeta(meta);

		if ((patient.getDeceased() instanceof BooleanType) && !((BooleanType) patient.getDeceased()).booleanValue()) {
			patient.setDeceased(null);
		}

		if (nhsPatient == null) {
			return patient;
		}

		patient = mappers.stream()
				.reduce(patient,
						(currentPatient, patientFieldMapper) -> patientFieldMapper.enhance(currentPatient, nhsPatient),
						(currentPatient, formerPatient) -> currentPatient
				);

		return patient;
	}
	
	public NhsPatient toNhsPatient(Patient patient, long patientId) {
		NhsPatient nhsPatient = new NhsPatient();

		nhsPatient = mappers.stream()
				.reduce(nhsPatient,
						(currentNhsPatient, patientFieldMapper) -> patientFieldMapper.mapToNhsPatient(patient, currentNhsPatient),
						(currentPatient, formerPatient) -> currentPatient
				);

		nhsPatient.setId(patientId);

		return nhsPatient;
	}
}

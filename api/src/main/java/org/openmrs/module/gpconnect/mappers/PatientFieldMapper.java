package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public interface PatientFieldMapper {
	
	Patient enhance(Patient patient, NhsPatient nhsPatient);
	
	NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient);
}

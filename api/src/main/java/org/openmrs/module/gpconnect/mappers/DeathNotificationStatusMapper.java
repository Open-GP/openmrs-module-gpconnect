package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public class DeathNotificationStatusMapper implements PatientFieldMapper {
	
	CodeableConceptExtension extension;
	
	public DeathNotificationStatusMapper(CodeableConceptExtension extension) {
		this.extension = extension;
	}
	
	@Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        extension.createExtension(nhsPatient.deathNotificationStatus).ifPresent(patient::addExtension);
        return patient;
    }
	
	@Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        extension.getValue(patient).ifPresent(nhsPatient::setDeathNotificationStatus);
        return nhsPatient;
    }
}

package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public class TreatmentCategoryMapper implements PatientFieldMapper {
	
	CodeableConceptExtension extension;
	
	public TreatmentCategoryMapper(CodeableConceptExtension extension) {
		this.extension = extension;
	}
	
	@Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        extension.createExtension(nhsPatient.treatmentCategory).ifPresent(patient::addExtension);
        return patient;
    }
	
	@Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        extension.getValue(patient).ifPresent(nhsPatient::setTreatmentCategory);
        return nhsPatient;
    }
}

package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public class ResidentialStatusMapper implements PatientFieldMapper {
	
	CodeableConceptExtension extension;
	
	public ResidentialStatusMapper(CodeableConceptExtension extension) {
		this.extension = extension;
	}
	
	@Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        extension.createExtension(nhsPatient.residentialStatus).ifPresent(patient::addExtension);

        return patient;
    }
	
	@Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        extension.getValue(patient).ifPresent(nhsPatient::setResidentialStatus);

        return  nhsPatient;
    }
}

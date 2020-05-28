package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public class ResidentialStatusMapper implements PatientFieldMapper {
    @Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        CodeableConceptExtension.RESIDENTIAL_STATUS.createExtension(nhsPatient.residentialStatus).ifPresent(patient::addExtension);

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        CodeableConceptExtension.RESIDENTIAL_STATUS.getValue(patient).ifPresent(nhsPatient::setResidentialStatus);

        return  nhsPatient;
    }
}

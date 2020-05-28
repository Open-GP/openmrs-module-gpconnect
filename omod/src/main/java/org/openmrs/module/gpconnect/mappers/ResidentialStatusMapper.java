package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.util.Extensions;

public class ResidentialStatusMapper implements PatientFieldMapper {
    @Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        if (nhsPatient.residentialStatus == null) {
            return patient;
        }

        try {
            ResidentialStatus residentialStatusEnum = ResidentialStatus.valueOf(nhsPatient.residentialStatus);
            CodeableConcept residentialStatus = new CodeableConcept();
            residentialStatus.addCoding(residentialStatusEnum.getCoding());
            Extension residentialStatusExt = new Extension(Extensions.RESIDENTIAL_STATUS_URL, residentialStatus);
            patient.addExtension(residentialStatusExt);
        } catch (IllegalArgumentException e) {
            System.out.printf("The residential status: %s is not a known one\n", nhsPatient.residentialStatus);
        }

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        CodeableConceptExtension.RESIDENTIAL_STATUS.getValue(patient).ifPresent(nhsPatient::setResidentialStatus);

        return  nhsPatient;
    }
}

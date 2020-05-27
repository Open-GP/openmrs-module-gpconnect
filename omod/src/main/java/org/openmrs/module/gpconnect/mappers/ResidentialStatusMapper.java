package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.ResidentialStatus;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;

import java.util.List;

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
            Extension residentialStatusExt = new Extension(GPConnectExtensions.RESIDENTIAL_STATUS_URL, residentialStatus);
            patient.addExtension(residentialStatusExt);
        } catch (IllegalArgumentException e) {
            System.out.printf("The residential status: %s is not a known one\n", nhsPatient.residentialStatus);
        }

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        List<Extension> residentialStatusExtensions = patient.getExtensionsByUrl(GPConnectExtensions.RESIDENTIAL_STATUS_URL);
        if (residentialStatusExtensions.size() > 0) {
            Coding coding = ((CodeableConcept) residentialStatusExtensions.get(0).getValue()).getCoding().get(0);

            if (coding.getSystem().equals(CodeSystems.RESIDENTIAL_STATUS) && ResidentialStatus.isValid(coding.getCode())) {
                nhsPatient.setResidentialStatus(coding.getCode());
            }

        }

        return  nhsPatient;
    }
}

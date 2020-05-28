package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public class EthnicCategoryMapper implements PatientFieldMapper{

    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        CodeableConceptExtension.ETHNIC_CATEGORY.createExtension(nhsPatient.ethnicCategory).ifPresent(patient::addExtension);

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        CodeableConceptExtension.ETHNIC_CATEGORY.getValue(patient).ifPresent(nhsPatient::setEthnicCategory);

        return  nhsPatient;
    }
}

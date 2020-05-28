package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;

public class EthnicCategoryMapper implements PatientFieldMapper{

    CodeableConceptExtension extension;

    public EthnicCategoryMapper(CodeableConceptExtension extension) {
        this.extension = extension;
    }

    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        extension.createExtension(nhsPatient.ethnicCategory).ifPresent(patient::addExtension);

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        extension.getValue(patient).ifPresent(nhsPatient::setEthnicCategory);

        return  nhsPatient;
    }
}

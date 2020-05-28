package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.EthnicCategory;
import org.openmrs.module.gpconnect.util.Extensions;

public class EthnicCategoryMapper implements PatientFieldMapper{

    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        if (nhsPatient.ethnicCategory == null) {
            return patient;
        }

        try {
            EthnicCategory ethnicCategoryEnum = EthnicCategory.valueOf(nhsPatient.ethnicCategory);
            CodeableConcept ethnicConcept = new CodeableConcept();
            ethnicConcept.addCoding(ethnicCategoryEnum.getCoding());
            Extension ethnicCategory = new Extension(Extensions.ETHNIC_CATEGORY_URL, ethnicConcept);
            patient.addExtension(ethnicCategory);
        } catch (IllegalArgumentException e) {
            System.out.printf("The ethnic category: %s is not a known one\n", nhsPatient.ethnicCategory);
        }

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        CodeableConceptExtension.ETHNIC_CATEGORY.getValue(patient).ifPresent(nhsPatient::setEthnicCategory);

        return  nhsPatient;
    }
}

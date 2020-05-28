package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.TreatmentCategory;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.List;
import java.util.Optional;

public class TreatmentCategoryMapper implements PatientFieldMapper {
    @Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        if (nhsPatient.treatmentCategory == null) {
            return patient;
        }

        Optional<TreatmentCategory> optionalTreatmentCategory = TreatmentCategory.findByCode(nhsPatient.treatmentCategory);
        if (!optionalTreatmentCategory.isPresent()) {
            System.out.printf("The treatment category: %s is not a known one\n", nhsPatient.treatmentCategory);
            return patient;
        }
        CodeableConcept treatmentCategory = new CodeableConcept();
        treatmentCategory.addCoding(optionalTreatmentCategory.get().getCoding());
        Extension treatmentCategoryExt = new Extension(Extensions.TREATMENT_CATEGORY_URL, treatmentCategory);
        patient.addExtension(treatmentCategoryExt);

        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        List<Extension> treatmentCategoryExtensions = patient.getExtensionsByUrl(Extensions.TREATMENT_CATEGORY_URL);
        if (treatmentCategoryExtensions.size() > 0) {
            Coding coding = ((CodeableConcept) treatmentCategoryExtensions.get(0).getValue()).getCoding().get(0);

            if (coding.getSystem().equals(CodeSystems.TREATMENT_CATEGORY) && TreatmentCategory.isValid(coding.getCode())) {
                nhsPatient.setTreatmentCategory(coding.getCode());
            }

        }

        return nhsPatient;
    }
}

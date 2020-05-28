package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.valueSets.RegistrationType;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;

import java.util.List;

public class RegistrationDetailsMapper implements PatientFieldMapper {
    @Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        Extension extension = new Extension(GPConnectExtensions.REGISTRATION_DETAILS_URL);

        if (nhsPatient.registrationStart != null || nhsPatient.registrationEnd != null) {
            Period period = new Period();
            period.setStart(nhsPatient.registrationStart);
            period.setEnd(nhsPatient.registrationEnd);
            Extension periodExtension = new Extension(GPConnectExtensions.REGISTRATION_PERIOD, period);
            extension.addExtension(periodExtension);
        }

        try {
            RegistrationType typeEnum = RegistrationType.valueOf(nhsPatient.registrationType);
            CodeableConcept registrationType = new CodeableConcept();
            registrationType.addCoding(typeEnum.getCoding());
            Extension typeExtension = new Extension(GPConnectExtensions.REGISTRATION_TYPE, registrationType);
            extension.addExtension(typeExtension);
        } catch (IllegalArgumentException | NullPointerException e) {
            System.err.printf("The registration type: %s is not a known one. Error: %s\n", nhsPatient.registrationType, e.getMessage());
        }


        if (nhsPatient.preferredBranch != null) {
            Extension preferredBranchExt = new Extension(GPConnectExtensions.PREFERRED_BRANCH, new Reference(nhsPatient.preferredBranch));
            extension.addExtension(preferredBranchExt);
        }

        patient.addExtension(extension);
        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {

        List<Extension> extensions = patient.getExtensionsByUrl(GPConnectExtensions.REGISTRATION_DETAILS_URL);

        if (extensions.size() > 0) {
            Extension registrationDetailsExt = extensions.get(0);
            List<Extension> detailsExtensions = registrationDetailsExt.getExtensionsByUrl(GPConnectExtensions.REGISTRATION_PERIOD);

            if (detailsExtensions.size() > 0) {
                Extension registrationPeriodExt = detailsExtensions.get(0);

                nhsPatient.setRegistrationStart(((Period) registrationPeriodExt.getValue()).getStart());
                nhsPatient.setRegistrationEnd(((Period) registrationPeriodExt.getValue()).getEnd());
            }

            List<Extension> typeExtensions = registrationDetailsExt.getExtensionsByUrl(GPConnectExtensions.REGISTRATION_TYPE);

            if (typeExtensions.size() > 0) {
                Coding coding = ((CodeableConcept) typeExtensions.get(0).getValue()).getCoding().get(0);
                if (coding.getSystem().equals(CodeSystems.REGISTRATION_TYPE) && RegistrationType.isValid(coding.getCode())) {
                    nhsPatient.setRegistrationType(coding.getCode());
                }
            }

            List<Extension> preferredBranchExtensions = registrationDetailsExt.getExtensionsByUrl(GPConnectExtensions.PREFERRED_BRANCH);

            if (preferredBranchExtensions.size() > 0) {
                String code = ((Reference) preferredBranchExtensions.get(0).getValue()).getReference();
                nhsPatient.setPreferredBranch(code);
            }
        }

        return nhsPatient;
    }
}

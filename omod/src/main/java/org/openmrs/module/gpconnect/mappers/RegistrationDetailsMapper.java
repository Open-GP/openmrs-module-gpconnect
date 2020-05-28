package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.List;

public class RegistrationDetailsMapper implements PatientFieldMapper {

    CodeableConceptExtension codeableConceptExtension;

    public RegistrationDetailsMapper(CodeableConceptExtension codeableConceptExtension) {
        this.codeableConceptExtension = codeableConceptExtension;
    }

    @Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        Extension extension = new Extension(Extensions.REGISTRATION_DETAILS_URL);

        if (nhsPatient.registrationStart != null || nhsPatient.registrationEnd != null) {
            Period period = new Period();
            period.setStart(nhsPatient.registrationStart);
            period.setEnd(nhsPatient.registrationEnd);
            Extension periodExtension = new Extension(Extensions.REGISTRATION_PERIOD, period);
            extension.addExtension(periodExtension);
        }

        codeableConceptExtension.createExtension(nhsPatient.registrationType).ifPresent(extension::addExtension);

        if (nhsPatient.preferredBranch != null) {
            Extension preferredBranchExt = new Extension(Extensions.PREFERRED_BRANCH, new Reference(nhsPatient.preferredBranch));
            extension.addExtension(preferredBranchExt);
        }

        patient.addExtension(extension);
        return patient;
    }

    @Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {

        List<Extension> extensions = patient.getExtensionsByUrl(Extensions.REGISTRATION_DETAILS_URL);

        if (extensions.size() > 0) {
            Extension registrationDetailsExt = extensions.get(0);
            List<Extension> detailsExtensions = registrationDetailsExt.getExtensionsByUrl(Extensions.REGISTRATION_PERIOD);

            if (detailsExtensions.size() > 0) {
                Extension registrationPeriodExt = detailsExtensions.get(0);

                nhsPatient.setRegistrationStart(((Period) registrationPeriodExt.getValue()).getStart());
                nhsPatient.setRegistrationEnd(((Period) registrationPeriodExt.getValue()).getEnd());
            }

            codeableConceptExtension.getValue(registrationDetailsExt).ifPresent(nhsPatient::setRegistrationType);

            List<Extension> preferredBranchExtensions = registrationDetailsExt.getExtensionsByUrl(Extensions.PREFERRED_BRANCH);

            if (preferredBranchExtensions.size() > 0) {
                String code = ((Reference) preferredBranchExtensions.get(0).getValue()).getReference();
                nhsPatient.setPreferredBranch(code);
            }
        }

        return nhsPatient;
    }
}

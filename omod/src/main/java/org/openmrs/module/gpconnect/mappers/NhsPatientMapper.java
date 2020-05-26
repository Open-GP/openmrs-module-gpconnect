package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.ContactPoint;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.GPConnectExtensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NhsPatientMapper {

    @Autowired
    NhsPatientService nhsPatientService;

    @Autowired
    PatientService patientService;

    public Patient enhance(Patient patient, String uuid) {
        if (patient == null) {
            return null;
        }
        ContactPoint email = new ContactPoint();
        email.setSystem(ContactPoint.ContactPointSystem.EMAIL).setValue("test@mail.com");
        patient.addTelecom(email);

        org.openmrs.Patient omrsPatient = patientService.getPatientByUuid(uuid);

        if (omrsPatient == null) {
            return patient;
        }

        NhsPatient nhsPatient = nhsPatientService.findById(omrsPatient.getPatientId().longValue());

        if (nhsPatient == null) {
            return patient;
        }

        Extension cadavericDonor = new Extension(GPConnectExtensions.CADAVERIC_DONOR_URL, new BooleanType(
                nhsPatient.cadavericDonor));
        patient.addExtension(cadavericDonor);

        return patient;
    }
}

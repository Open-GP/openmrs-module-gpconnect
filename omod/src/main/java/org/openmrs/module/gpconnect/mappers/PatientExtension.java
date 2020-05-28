package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.Optional;

public interface PatientExtension<T> {
    Optional<Extension> createExtension(T value);
    Optional<T> getValue(Patient patient);
    Optional<T> getValue(Extension extension);
}

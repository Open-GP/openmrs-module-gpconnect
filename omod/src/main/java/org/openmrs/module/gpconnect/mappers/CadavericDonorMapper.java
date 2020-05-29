package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

public class CadavericDonorMapper implements PatientFieldMapper {
	
	private final BooleanExtension extension;
	
	public CadavericDonorMapper(BooleanExtension extension) {
		this.extension = extension;
	}
	
	@Override
    public Patient enhance(Patient patient, NhsPatient nhsPatient) {
        extension.createExtension(nhsPatient.cadavericDonor).ifPresent(patient::addExtension);
        return patient;
    }
	
	@Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        new BooleanExtension(Extensions.CADAVERIC_DONOR_URL).getValue(patient).ifPresent(nhsPatient::setCadavericDonor);
        return nhsPatient;
    }
}

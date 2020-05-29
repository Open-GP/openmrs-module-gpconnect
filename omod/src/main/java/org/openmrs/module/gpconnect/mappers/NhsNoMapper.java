package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Optional;

public class NhsNoMapper implements PatientFieldMapper {
	
	CodeableConceptExtension extension;
	
	public NhsNoMapper(CodeableConceptExtension extension) {
		this.extension = extension;
	}
	
	@Override
	public Patient enhance(Patient patient, NhsPatient nhsPatient) {
		Identifier nhsNoIdentifier = new Identifier();
		nhsNoIdentifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
		nhsNoIdentifier.setValue(nhsPatient.nhsNumber);

		extension.createExtension(nhsPatient.nhsNumberVerificationStatus).ifPresent(nhsNoIdentifier::addExtension);

		patient.addIdentifier(nhsNoIdentifier);

		return patient;
	}
	
	@Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        Optional<Identifier> optionalNhsNo = patient.getIdentifier()
                .stream()
                .filter((identifier -> identifier.getSystem().equals(Extensions.NHS_NUMBER_SYSTEM)))
                .findFirst();

		optionalNhsNo.map(Identifier::getValue).ifPresent(nhsPatient::setNhsNumber);
        optionalNhsNo.flatMap(nhsNoIdentifier ->  extension.getValue(nhsNoIdentifier)).ifPresent(nhsPatient::setNhsNumberVerificationStatus);

        return nhsPatient;
    }
}

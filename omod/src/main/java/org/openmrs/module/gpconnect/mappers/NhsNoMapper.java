package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.Optional;

public class NhsNoMapper implements PatientFieldMapper {
	
	CodeableConceptExtension extension;
	
	public NhsNoMapper(CodeableConceptExtension extension) {
		this.extension = extension;
	}
	
	@Override
	public Patient enhance(Patient patient, NhsPatient nhsPatient) {

		Identifier oldNhsNoIdentifier = patient.getIdentifier()
				.stream()
				.filter(identifier -> identifier.getSystem().equals(Extensions.NHS_NUMBER_SYSTEM))
				.findFirst()
				.get();

		Identifier nhsNoIdentifier = new Identifier().setSystem(Extensions.NHS_NUMBER_SYSTEM).setValue(oldNhsNoIdentifier.getValue());
		extension.createExtension(nhsPatient.nhsNumberVerificationStatus).ifPresent(nhsNoIdentifier::addExtension);

		patient.setIdentifier(Collections.singletonList(nhsNoIdentifier));

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

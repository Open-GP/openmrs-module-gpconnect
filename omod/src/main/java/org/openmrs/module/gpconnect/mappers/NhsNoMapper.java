package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Identifier;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.StringType;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Collections;
import java.util.Optional;

public class NhsNoMapper implements PatientFieldMapper {
	
	//    CodeableConceptExtension extension;
	//
	//    public NhsNoMapper(CodeableConceptExtension extension) {
	//        this.extension = extension;
	//    }
	
	@Override
	public Patient enhance(Patient patient, NhsPatient nhsPatient) {
		Identifier nhsNoIdentifier = new Identifier();
		nhsNoIdentifier.setSystem(Extensions.NHS_NUMBER_SYSTEM);
		nhsNoIdentifier.setValue(nhsPatient.nhsNumber);
		Extension verficationStatus = new Extension(Extensions.NHS_VERFICATION_STATUS_URL, new StringType(
		        nhsPatient.nhsNumberVerificationStatus));
		nhsNoIdentifier.setExtension(Collections.singletonList(verficationStatus));
		
		patient.addIdentifier(nhsNoIdentifier);
		
		return patient;
	}
	
	@Override
    public NhsPatient mapToNhsPatient(Patient patient, NhsPatient nhsPatient) {
        Optional<Identifier> optionalNhsNo = patient.getIdentifier()
                .stream()
                .filter((identifier -> identifier.getSystem().equals(Extensions.NHS_NUMBER_SYSTEM)))
                .findFirst();

        if (optionalNhsNo.isPresent()) {
            nhsPatient.setNhsNumber(optionalNhsNo.get().getValue());
            nhsPatient.setNhsNumberVerificationStatus(
                    optionalNhsNo.get()
                            .getExtensionString(Extensions.NHS_VERFICATION_STATUS_URL));

        }

        return nhsPatient;
    }
}

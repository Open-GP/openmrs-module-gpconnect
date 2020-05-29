package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.BooleanType;
import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;

import java.util.List;
import java.util.Optional;

public class BooleanExtension implements PatientExtension<Boolean> {
	
	private final String extensionUrl;
	
	public BooleanExtension(String extensionUrl) {
		this.extensionUrl = extensionUrl;
	}
	
	@Override
	public Optional<Extension> createExtension(Boolean value) {
		Extension cadavericDonor = new Extension(extensionUrl, new BooleanType(value));
		return Optional.of(cadavericDonor);
	}
	
	@Override
	public Optional<Boolean> getValue(Patient patient) {
		List<Extension> extensionsByUrl = patient.getExtensionsByUrl(extensionUrl);
		return getValue(extensionsByUrl);
	}
	
	@Override
	public Optional<Boolean> getValue(Extension extension) {
		List<Extension> extensionsByUrl = extension.getExtensionsByUrl(extensionUrl);
		return getValue(extensionsByUrl);
	}
	
	private Optional<Boolean> getValue(List<Extension> extensionsByUrl) {
		if (extensionsByUrl.size() > 0) {
			return Optional.of(((BooleanType) extensionsByUrl.get(0).getValue()).booleanValue());
		}
		return Optional.empty();
	}
	
}

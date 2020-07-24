package org.openmrs.module.gpconnect.providers;

import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.BAD_REQUEST;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_IDENTIFIER_SYSTEM;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.INVALID_PARAMETER;
import static org.openmrs.module.gpconnect.exceptions.GPConnectCoding.PRACTITIONER_NOT_FOUND;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.BundleProviders;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Practitioner30_40;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.providers.r3.PractitionerFhirResourceProvider;
import org.openmrs.module.gpconnect.exceptions.GPConnectExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
@Primary
public class GPConnectPractitionerProvider extends PractitionerFhirResourceProvider {
	
	@Autowired
	private FhirPractitionerService practitionerService;
	
	@Operation(name = "$setup")
	public MethodOutcome setupPractitioner(
			@OperationParam(name = "practitioner", type = Practitioner.class) Practitioner practitioner) {
		practitionerService.create(Practitioner30_40.convertPractitioner(practitioner));
		return new MethodOutcome();
	}

	@Override
	@Read
	public Practitioner getPractitionerById(@IdParam @NotNull IdType id) {
		try {
			Practitioner practitioner = super.getPractitionerById(id);
			return addMeta(practitioner);
		}
		catch (ResourceNotFoundException e) {
			throw GPConnectExceptions.resourceNotFoundException(
				"No practitioner details found for practitioner ID: Practitioner/" + id.getIdPart(), PRACTITIONER_NOT_FOUND);
		}
	}

	@Override
	@Search
	public IBundleProvider searchForPractitioners(@OptionalParam(name = "name") StringAndListParam name,
		                                          @OptionalParam(name = "identifier") TokenAndListParam identifier,
												  @OptionalParam(name = "given") StringAndListParam given,
												  @OptionalParam(name = "family") StringAndListParam family,
												  @OptionalParam(name = "address-city") StringAndListParam city,
											      @OptionalParam(name = "address-state") StringAndListParam state,
											      @OptionalParam(name = "address-postalcode") StringAndListParam postalCode,
												  @OptionalParam(name = "address-country") StringAndListParam country,
												  @OptionalParam(name = "_id") TokenAndListParam id,
												  @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated) {
		validateIdentifierStructure(identifier);

		IBundleProvider provider = super.searchForPractitioners(name, identifier, null, null, null, null, null, null, null, null);
		List<IBaseResource> resources = provider.getResources(0, 0);

		List<IBaseResource> r3Practitioners = resources.stream()
				.map(iBaseResource -> Practitioner30_40.convertPractitioner((org.hl7.fhir.r4.model.Practitioner) iBaseResource))
				.map(this::addMeta)
				.collect(Collectors.toList());

		return BundleProviders.newList(r3Practitioners);
	}

	private void validateIdentifierStructure(TokenAndListParam identifier) {
		if (identifier == null || identifier.getValuesAsQueryTokens().size() != 1) {
			throw GPConnectExceptions.invalidRequestException("Exactly 1 identifier needs to be provided", BAD_REQUEST);
		}

		TokenParam tokenParam = identifier.getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
		String identifierSystem = tokenParam.getSystem();
		String identifierValue = tokenParam.getValue();

		if (identifierValue == null || identifierValue.isEmpty() || identifierSystem == null || identifierSystem.isEmpty() || identifierValue.split("\\|").length == 2) {
			throw GPConnectExceptions.unprocessableEntityException(
				String.format("One or both of the identifier system and value are missing from given identifier : %s|%s", identifierSystem, identifierValue), INVALID_PARAMETER);
		}

		if (!identifierSystem.equals("https://fhir.nhs.uk/Id/sds-user-id")) {
			throw GPConnectExceptions.invalidRequestException("The given identifier system code (" + identifierSystem + ") is not an expected code", INVALID_IDENTIFIER_SYSTEM);
		}

		if (identifierValue.split(",").length == 2) {
			throw GPConnectExceptions.invalidRequestException("Multiple values detected for non-repeatable parameter 'identifier'."
				+ "This server is not configured to allow multiple (AND/OR) values for this param.", BAD_REQUEST);
		}
	}

	private Practitioner addMeta(Practitioner practitioner) {
		Meta meta = new Meta().setProfile(
		    Collections.singletonList(new UriType(
		            "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Practitioner-1"))).setVersionId(
		    String.format("%s-1", practitioner.getId()));
		
		practitioner.setMeta(meta);
		return practitioner;
	}
}

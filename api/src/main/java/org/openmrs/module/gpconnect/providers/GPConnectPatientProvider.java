package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.annotation.Sort;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.BundleProviders;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.providers.r3.PatientFhirResourceProvider;
import org.openmrs.module.gpconnect.exceptions.OperationOutcomeCreator;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.GPConnectPatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Qualifier("fhirR3Resources")
@Setter(AccessLevel.PACKAGE)
@Primary
public class GPConnectPatientProvider extends PatientFhirResourceProvider {

	@Autowired
	private FhirPatientService patientService;

	@Autowired
	NhsPatientMapper nhsPatientMapper;

	@Autowired
	private FhirPatientDao patientDao;

	@Autowired
	private GPConnectPatientService gpConnectPatientService;

	@Override
	@Read
	public Patient getPatientById(@IdParam @NotNull IdType id) {
		try {
			Patient patient = super.getPatientById(id);
			return nhsPatientMapper.enhance(patient);
		}
		catch (ResourceNotFoundException e) {
			throw patientNotFoundFhirException(id.getIdPart());
		}
	}

	@Operation(name = "$gpc.registerpatient")
	public Bundle registerPatient(@OperationParam(name = "registerPatient", type = Patient.class) Patient patient) {
		org.openmrs.Patient newPatient = gpConnectPatientService.save(patient);

		Patient createdPatient = this.getPatientById(new IdType(newPatient.getUuid()));

		return searchSetBundleWith(createdPatient);
	}

	private Bundle searchSetBundleWith(Patient createdPatient) {
		Bundle bundle = new Bundle();
		Bundle.BundleEntryComponent entryComponent = new Bundle.BundleEntryComponent();
		entryComponent.setResource(createdPatient);
		bundle.addEntry(entryComponent);
		bundle.setType(Bundle.BundleType.SEARCHSET);
		bundle.setMeta(new Meta().setProfile(Collections.singletonList(new UriType(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-Searchset-Bundle-1"))));
		return bundle;
	}

	@Search
	@SuppressWarnings("unused")
	public IBundleProvider searchPatients(@OptionalParam(name = "name") StringAndListParam name,
										  @OptionalParam(name = "given") StringAndListParam given,
                                          @OptionalParam(name = "family") StringAndListParam family,
                                          @OptionalParam(name = "identifier") TokenAndListParam identifier,
                                          @OptionalParam(name = "gender") TokenAndListParam gender,
                                          @OptionalParam(name = "birthdate") DateRangeParam birthDate,
                                          @OptionalParam(name = "death-date") DateRangeParam deathDate,
                                          @OptionalParam(name = "deceased") TokenAndListParam deceased,
                                          @OptionalParam(name = "address-city") StringAndListParam city,
                                          @OptionalParam(name = "address-state") StringAndListParam state,
                                          @OptionalParam(name = "address-postalcode") StringAndListParam postalCode,
                                          @OptionalParam(name = "address-country") StringAndListParam country,
                                          @OptionalParam(name = "_id") TokenAndListParam id,
                                          @OptionalParam(name = "_lastUpdated") DateRangeParam lastUpdated,
                                          @Sort SortSpec sort) {

		validateIdentifierStructure(identifier);

		IBundleProvider provider = super.searchPatients(name, given, family, identifier, gender, birthDate,
				deathDate, deceased, city, state, postalCode, country, id, lastUpdated, sort);

		List<IBaseResource> resources = provider.getResources(0, 0);

		List<IBaseResource> r3Patients = resources.stream()
				.map(iBaseResource -> Patient30_40.convertPatient((org.hl7.fhir.r4.model.Patient) iBaseResource))
				.map(patient -> nhsPatientMapper.enhance(patient))
				.filter(patient -> patient.getDeceasedDateTimeType() == null)
				.collect(Collectors.toList());

		return BundleProviders.newList(r3Patients);
	}

	private InvalidRequestException createBadRequest(String errorMessage, String errorCode) {
		OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, errorCode, OperationOutcome.IssueType.INVALID);
		return new InvalidRequestException(errorMessage, operationOutcome);
	}

	private ResourceNotFoundException patientNotFoundFhirException(String id) {
		String errorMessage = "No patient details found for patient ID: Patient/" + id;
		OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "PATIENT_NOT_FOUND", OperationOutcome.IssueType.INVALID);
		return new ResourceNotFoundException(errorMessage, operationOutcome);
	}

	private UnprocessableEntityException createMissingIdentifierPartException(String identifier) {
		String errorMessage = String.format("One or both of the identifier system and value are missing from given identifier : %s", identifier);
		OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "INVALID_PARAMETER", OperationOutcome.IssueType.INVALID);
		return new UnprocessableEntityException(errorMessage, operationOutcome);
	}

	private void validateIdentifierStructure(TokenAndListParam identifier) {
		if (identifier != null) {

			List<TokenOrListParam> identifierParams = identifier.getValuesAsQueryTokens();

			if (identifierParams.size() > 1){
				throw createBadRequest("Too many indentifiers", "BAD_REQUEST");
			}

			TokenParam tokenParam = identifierParams.get(0).getValuesAsQueryTokens().get(0);
			String identifierTypeName = tokenParam.getSystem();
			String identifierValue = tokenParam.getValue();

			if (identifierTypeName == null || identifierTypeName.isEmpty()) {
				throw createMissingIdentifierPartException(identifierValue);
			}

			if (identifierValue.isEmpty()) {
				throw createMissingIdentifierPartException(identifierTypeName + "|");
			}

			if (patientService.getPatientIdentifierTypeByIdentifier(new Identifier().setSystem(identifierTypeName)) == null){
				String errorMessage = String.format("The given identifier system code (%s) is not an expected code", identifierTypeName);
				OperationOutcome operationOutcome = OperationOutcomeCreator.build(errorMessage, "INVALID_IDENTIFIER_SYSTEM", OperationOutcome.IssueType.INVALID);
				throw new InvalidRequestException(errorMessage, operationOutcome);
			}
		}
	}
}

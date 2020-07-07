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
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Patient30_40;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Identifier;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.fhir2.providers.r3.PatientFhirResourceProvider;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.services.NhsPatientService;
import org.openmrs.module.gpconnect.util.CodeSystems;
import org.openmrs.module.gpconnect.util.Extensions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
	private NhsPatientService nhsPatientService;

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
		if (patient.getIdentifier().isEmpty()) {
			throw createBadRequest("Patient is missing id", "INVALID_NHS_NUMBER");
		}

		String nhsNumber = patient.getIdentifier().get(0).getValue();

		if (nhsNumber.length() != 10) {
			throw createBadRequest("NHS Number is invalid", "INVALID_NHS_NUMBER");
		}

		Collection<org.openmrs.Patient> patients = findByNhsNumber(nhsNumber);
		if (patients.size() > 0) {
			if(patients.iterator().next().getDead()){
				throw createBadRequest("Nhs Number registed to dead patient", "BAD_REQUEST");
			}

			Coding invalidIdentifierCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "DUPLICATE_REJECTED",
					"DUPLICATE_REJECTED");

			String message = "Nhs Number already in use";
			throw new ResourceVersionConflictException(message, createErrorOperationOutcome(
					message, invalidIdentifierCoding, OperationOutcome.IssueType.INVALID));
		}

		if (patient.getBirthDate() == null) {
			throw createBadRequest("Birth date is mandatory", "BAD_REQUEST");
		}

		if (!hasValidNames(patient)) {
			throw createBadRequest("Patient must have an official name containing at least a family name", "BAD_REQUEST");
		}

		org.hl7.fhir.r4.model.Patient receivedPatient = Patient30_40.convertPatient(patient);
		patientService.create(receivedPatient);

		org.openmrs.Patient newPatient = findByNhsNumber(nhsNumber).iterator().next();

		NhsPatient nhsPatient = nhsPatientMapper.toNhsPatient(patient, newPatient.getPatientId());
		nhsPatientService.saveOrUpdate(nhsPatient);

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

	private boolean hasValidNames(Patient patient) {
		Optional<HumanName> officialName = patient.getName().stream().filter(humanName -> humanName.getUse().equals(HumanName.NameUse.OFFICIAL)).findFirst();
		return officialName.map(humanName -> (humanName.getFamily() != null) && (!humanName.getFamily().isEmpty())).orElse(false);
	}

	private Collection<org.openmrs.Patient> findByNhsNumber(String nhsNumber) {
		TokenAndListParam identifier = new TokenAndListParam()
		        .addAnd(new TokenParam(Extensions.NHS_NUMBER_SYSTEM, nhsNumber));

		SearchParameterMap params = (new SearchParameterMap()).addParameter("identifier.search.handler", identifier);

		List<String> resultUuids = patientDao.getResultUuids(params);
		Collection<org.openmrs.Patient> patients = patientDao.search(params, resultUuids);
		return patients;
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


		if (identifier == null) {
			throw createBadRequest("Missing identifier param", "BAD_REQUEST");
		}

		List<TokenOrListParam> identifierParams = identifier.getValuesAsQueryTokens();

		if (identifierParams.size() > 1) {
			throw createBadRequest("Too many indentifiers", "BAD_REQUEST");
		}

		TokenParam tokenParam = identifierParams.get(0).getValuesAsQueryTokens().get(0);
		String identifierTypeName = tokenParam.getSystem();
		String identifierValue = tokenParam.getValue();

		if (identifierValue.isEmpty()) {
			throw createMissingIdentifierPartException(identifierTypeName + "|");
		}

		if (identifierTypeName == null || identifierTypeName.isEmpty()) {
			throw createMissingIdentifierPartException(identifierValue);
		}

		if (patientService.getPatientIdentifierTypeByIdentifier(new Identifier().setSystem(identifierTypeName)) == null){
			String errorMessage = String.format("The given identifier system code (%s) is not an expected code", identifierTypeName);
			Coding invalidIdentifierCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "INVALID_IDENTIFIER_SYSTEM", "INVALID_IDENTIFIER_SYSTEM");
			OperationOutcome invalidIdentifier = createErrorOperationOutcome(errorMessage, invalidIdentifierCoding, OperationOutcome.IssueType.INVALID);
			throw new InvalidRequestException(errorMessage, invalidIdentifier);
		}

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
		Coding invalidIdentifierCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, errorCode, errorCode);
		OperationOutcome badRequest = createErrorOperationOutcome(errorMessage, invalidIdentifierCoding,
		    OperationOutcome.IssueType.INVALID);
		return new InvalidRequestException(errorMessage, badRequest);
	}

	private OperationOutcome createErrorOperationOutcome(String errorMessage, Coding coding,
	        OperationOutcome.IssueType issueType) {
		OperationOutcome patientNotFound = new OperationOutcome();
		Meta meta = new Meta();
		meta.setProfile(Collections.singletonList(new UriType(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1")));

		patientNotFound.setMeta(meta);

		OperationOutcome.OperationOutcomeIssueComponent issue = new OperationOutcome.OperationOutcomeIssueComponent();
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setCode(issueType);
		CodeableConcept details = new CodeableConcept().setCoding(Collections.singletonList(coding));
		issue.setDetails(details);
		issue.setDiagnostics(errorMessage);
		patientNotFound.setIssue(Collections.singletonList(issue));
		return patientNotFound;
	}

	private ResourceNotFoundException patientNotFoundFhirException(String id) {
		String errorMessage = "No patient details found for patient ID: Patient/" + id;
		Coding notFoundCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "PATIENT_NOT_FOUND", "PATIENT_NOT_FOUND");
		OperationOutcome patientNotFound = createErrorOperationOutcome(errorMessage, notFoundCoding,
		    OperationOutcome.IssueType.INVALID);
		return new ResourceNotFoundException(errorMessage, patientNotFound);
	}

	private UnprocessableEntityException createMissingIdentifierPartException(String identifier) {
		String errorMessage = String.format(
		    "One or both of the identifier system and value are missing from given identifier : %s", identifier);
		Coding coding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "INVALID_PARAMETER", "INVALID_PARAMETER");
		OperationOutcome operationOutcome = createErrorOperationOutcome(errorMessage, coding,
		    OperationOutcome.IssueType.INVALID);
		return new UnprocessableEntityException(errorMessage, operationOutcome);
	}
}

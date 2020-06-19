package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import ca.uhn.fhir.rest.annotation.OptionalParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.server.BundleProviders;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.Setter;
import org.hl7.fhir.convertors.conv30_40.Practitioner30_40;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Coding;
import org.hl7.fhir.dstu3.model.IdType;
import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.hl7.fhir.dstu3.model.UriType;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.openmrs.module.fhir2.providers.r3.PractitionerFhirResourceProvider;
import org.openmrs.module.gpconnect.util.CodeSystems;
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
			
			Meta meta = new Meta().setProfile(
			    Collections.singletonList(new UriType(
			            "https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Practitioner-1"))).setVersionId(
			    String.format("%s-1", id.getIdPart()));
			
			practitioner.setMeta(meta);
			
			return practitioner;
		}
		catch (ResourceNotFoundException e) {
			String errorMessage = "No practitioner details found for practitioner ID: Practitioner/" + id.getIdPart();
			Coding notFoundCoding = new Coding(CodeSystems.SPINE_ERROR_OR_WARNING_CODE, "PRACTITIONER_NOT_FOUND",
			        "PRACTITIONER_NOT_FOUND");
			OperationOutcome practitionerNotFound = createErrorOperationOutcome(errorMessage, notFoundCoding,
			    OperationOutcome.IssueType.INVALID);
			throw new ResourceNotFoundException(errorMessage, practitionerNotFound);
		}
	}
	
	@Search
	public IBundleProvider searchForPractitioners(@OptionalParam(name = "name") StringAndListParam name, @OptionalParam(name = "identifier") TokenAndListParam identifier) {
		IBundleProvider provider = super.searchForPractitioners(name, identifier);
		List<IBaseResource> resources = provider.getResources(0, 0);

		List<IBaseResource> r3Practitioners = resources.stream()
				.map(iBaseResource -> Practitioner30_40.convertPractitioner((org.hl7.fhir.r4.model.Practitioner) iBaseResource))
				.collect(Collectors.toList());

		return BundleProviders.newList(r3Practitioners);
	}
	
	private OperationOutcome createErrorOperationOutcome(String errorMessage, Coding coding,
	        OperationOutcome.IssueType issueType) {
		OperationOutcome operationOutcome = new OperationOutcome();
		Meta meta = new Meta();
		meta.setProfile(Collections.singletonList(new UriType(
		        "https://fhir.nhs.uk/STU3/StructureDefinition/GPConnect-OperationOutcome-1")));
		
		operationOutcome.setMeta(meta);
		
		OperationOutcome.OperationOutcomeIssueComponent issue = new OperationOutcome.OperationOutcomeIssueComponent();
		issue.setSeverity(OperationOutcome.IssueSeverity.ERROR);
		issue.setCode(issueType);
		CodeableConcept details = new CodeableConcept().setCoding(Collections.singletonList(coding));
		issue.setDetails(details);
		issue.setDiagnostics(errorMessage);
		operationOutcome.setIssue(Collections.singletonList(issue));
		return operationOutcome;
	}
	
}

/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hamcrest.Description;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.web.servlet.FhirRestServlet;
import org.openmrs.module.gpconnect.server.GPConnectServer;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.UnsupportedEncodingException;
import java.util.stream.Collectors;

public abstract class BaseFhirR3ResourceProviderWebTest<T extends IResourceProvider, U extends IBaseResource> extends BaseFhirResourceProviderWebTest<T, U> {
	
	private static final FhirContext FHIR_CONTEXT = FhirContext.forDstu3();

	@Override
	public String getServletName() {
		return "gpconnect/gpconnectServlet";
	}
	
	@Override
	public FhirContext getFhirContext() {
		return FHIR_CONTEXT;
	}
	
	@Override
	public FhirRestServlet getRestfulServer() {
		return new GPConnectServer();
	}
	
	@Override
	public void describeOperationOutcome(Description mismatchDescription, IBaseOperationOutcome baseOperationOutcome) {
		if (baseOperationOutcome instanceof OperationOutcome) {
			OperationOutcome operationOutcome = (OperationOutcome) baseOperationOutcome;
			if (operationOutcome.hasIssue() && operationOutcome.getIssue().stream()
			        .anyMatch(o -> o.getSeverity().ordinal() <= OperationOutcome.IssueSeverity.WARNING.ordinal())) {
				mismatchDescription.appendText(" with message ");
				mismatchDescription.appendValue(operationOutcome.getIssue().stream()
				        .filter(o -> o.getSeverity().ordinal() <= OperationOutcome.IssueSeverity.WARNING.ordinal())
				        .map(OperationOutcome.OperationOutcomeIssueComponent::getDiagnostics)
				        .collect(Collectors.joining(". ")));
			}
		}
	}
	
	@Override
	public Class<? extends IBaseOperationOutcome> getOperationOutcomeClass() {
		return OperationOutcome.class;
	}
	
	@Override
	public Bundle readBundleResponse(MockHttpServletResponse response) throws UnsupportedEncodingException {
		return (Bundle) super.readBundleResponse(response);
	}
}

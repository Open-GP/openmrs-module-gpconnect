package org.openmrs.module.gpconnect.server;

import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.openmrs.module.fhir2.providers.r3.LocationFhirResourceProvider;
import org.openmrs.module.fhir2.providers.r3.PatientFhirResourceProvider;
import org.openmrs.module.fhir2.providers.r3.PractitionerFhirResourceProvider;
import org.openmrs.module.fhir2.web.servlet.FhirR3RestServlet;
import org.openmrs.module.gpconnect.interceptors.GPConnectExceptionHandlingInterceptor;
import org.openmrs.module.gpconnect.interceptors.IdentifierInterceptor;
import org.openmrs.module.gpconnect.interceptors.InteractionIdInterceptor;
import org.openmrs.module.gpconnect.interceptors.PatientSearchRequestInterceptor;
import org.openmrs.module.gpconnect.providers.GPConnectLocationProvider;
import org.openmrs.module.gpconnect.providers.GPConnectPatientProvider;
import org.openmrs.module.gpconnect.providers.GPConnectPractitionerProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class GPConnectServer extends FhirR3RestServlet {
	
	@Override
	protected void initialize() {
		super.initialize();
		
		GPConnectExceptionHandlingInterceptor exceptionInterceptor = new GPConnectExceptionHandlingInterceptor();
		registerInterceptor(exceptionInterceptor);

		InteractionIdInterceptor interactionIdInterceptor = new InteractionIdInterceptor();
		registerInterceptor(interactionIdInterceptor);

		PatientSearchRequestInterceptor patientSearchRequestInterceptor = new PatientSearchRequestInterceptor();
		registerInterceptor(patientSearchRequestInterceptor);

		IdentifierInterceptor identifierInterceptor = new IdentifierInterceptor();
		registerInterceptor(identifierInterceptor);
	}
	
	@Autowired
	@Qualifier("fhirR3Resources")
	@Override
	public void setResourceProviders(Collection<IResourceProvider> theProviders) {
		List<IResourceProvider> filteredProviders =
				theProviders.stream()
						.filter( iResourceProvider ->
								!(iResourceProvider instanceof PatientFhirResourceProvider) || iResourceProvider instanceof GPConnectPatientProvider)
						.filter( iResourceProvider ->
								!(iResourceProvider instanceof LocationFhirResourceProvider) || iResourceProvider instanceof GPConnectLocationProvider)
						.filter( iResourceProvider ->
								!(iResourceProvider instanceof PractitionerFhirResourceProvider) || iResourceProvider instanceof GPConnectPractitionerProvider)
						.collect(Collectors.toList());

		super.setResourceProviders(filteredProviders);
	}
	
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath.substring(escapedLength(servletContextPath) + escapedLength(servletPath)
		        + escapedLength("/gpconnect/gpconnectServlet"));
	}
}

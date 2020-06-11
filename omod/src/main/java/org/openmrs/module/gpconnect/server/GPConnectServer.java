package org.openmrs.module.gpconnect.server;

import ca.uhn.fhir.rest.server.IResourceProvider;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.module.fhir.server.FHIRRESTServer;

import java.util.ArrayList;
import java.util.List;

public class GPConnectServer extends FHIRRESTServer {
	
	private static final long serialVersionUID = 1L;
	
	private static final String MODULE_SERVELET_PREFIX = "/gpconnect/gpconnectServlet";
	
	/**
	 * The initialize method is automatically called when the servlet is starting up, so it can be
	 * used to configure the servlet to define resource providers, or set up configuration,
	 * interceptors, etc.
	 */
	@Override
	protected void initialize() {
		new AdministrationServiceImpl().setGlobalProperty("fhir.patient.strategy", "GPConnectPatientStrategy");
		
		super.initialize();
		
		List<IResourceProvider> additionalResourceProviders = new ArrayList<IResourceProvider>();
		additionalResourceProviders.add(new PatientOperationsProvider());
		
		registerProviders(additionalResourceProviders);
	}
	
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath.substring(escapedLength(servletContextPath) + escapedLength(servletPath)
		        + escapedLength(MODULE_SERVELET_PREFIX));
	}
}

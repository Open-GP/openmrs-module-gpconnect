package org.openmrs.module.gpconnect.server;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.narrative.CustomThymeleafNarrativeGenerator;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.interceptor.LoggingInterceptor;
import ca.uhn.fhir.rest.server.interceptor.ResponseHighlighterInterceptor;
import org.hl7.fhir.dstu3.hapi.rest.server.ServerCapabilityStatementProvider;
import org.openmrs.api.impl.AdministrationServiceImpl;
import org.openmrs.module.fhir.addressstrategy.OpenMRSFHIRRequestAddressStrategy;
import org.openmrs.module.fhir.api.util.FHIRUtils;
import org.openmrs.module.fhir.providers.RestfulPatientResourceProvider;
import org.openmrs.module.fhir.server.ConformanceProvider;
import org.openmrs.module.gpconnect.util.GPConnectConstants;

import java.util.ArrayList;
import java.util.List;

public class GPConnectServer extends RestfulServer {
	
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
		this.setServerAddressStrategy(new OpenMRSFHIRRequestAddressStrategy());
		List<IResourceProvider> resourceProviders = new ArrayList<IResourceProvider>();
		resourceProviders.add(new RestfulPatientResourceProvider());
		this.setFhirContext(FhirContext.forDstu3());
		setResourceProviders(resourceProviders);
		setServerName(GPConnectConstants.GPCONNECT_SERVER_NAME);
		setImplementationDescription(GPConnectConstants.GPCONNECT_SERVER_DES);
		setDefaultPrettyPrint(true);
		setDefaultResponseEncoding(EncodingEnum.JSON);
		if (FHIRUtils.isCustomNarrativesEnabled()) {
			String propFile = FHIRUtils.getCustomNarrativesPropertyPath();
			CustomThymeleafNarrativeGenerator generator = new CustomThymeleafNarrativeGenerator(propFile);
			getFhirContext().setNarrativeGenerator(generator);
		}
		ResponseHighlighterInterceptor responseHighlighter = new ResponseHighlighterInterceptor();
		registerInterceptor(responseHighlighter);
		LoggingInterceptor loggingInterceptor = new LoggingInterceptor();
		registerInterceptor(loggingInterceptor);
		loggingInterceptor.setLoggerName("test.accesslog");
		loggingInterceptor.setMessageFormat("Source[${remoteAddr}] Operation[${operationType} ${idOrResourceName}] "
		        + "UA[${requestHeader.user-agent}] Params[${requestParameters}]");
		ServerCapabilityStatementProvider sc = new ServerCapabilityStatementProvider(this);
		this.setServerConformanceProvider(sc);
		ConformanceProvider provider = new ConformanceProvider();
		provider.setRestfulServer(this);
	}
	
	protected String getRequestPath(String requestFullPath, String servletContextPath, String servletPath) {
		return requestFullPath.substring(escapedLength(servletContextPath) + escapedLength(servletPath)
		        + escapedLength(MODULE_SERVELET_PREFIX));
	}
}

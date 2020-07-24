package org.openmrs.module.gpconnect.interceptors;

import ca.uhn.fhir.interceptor.api.Hook;
import ca.uhn.fhir.interceptor.api.Pointcut;

public class InvalidUrlInterceptor {

    @Hook(Pointcut.SERVER_PRE_PROCESS_OUTGOING_EXCEPTION)
    public void handleInvalidUrl(){

    }


}

package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.server.BundleProviders;
import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPractitionerService;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPractitionerProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GPConnectPractitionerProvider, Practitioner> {

    @Mock
    private FhirPractitionerService practitionerService;

    @Getter(AccessLevel.PUBLIC)
    @InjectMocks
    GPConnectPractitionerProvider resourceProvider;

    @Test
    public void shouldSearchByIdentifier() throws IOException, ServletException {
        String identifier = "G11111111";

        IBundleProvider provider = BundleProviders.newList(new org.hl7.fhir.r4.model.Practitioner());
        when(practitionerService.searchForPractitioners(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any())).thenReturn(provider);

        MockHttpServletResponse response = get("/Practitioner?identifier=https://fhir.nhs.uk/Id/sds-user-id|" + identifier).accept(FhirMediaTypes.JSON).go();

        assertThat(response, isOk());
    }
}

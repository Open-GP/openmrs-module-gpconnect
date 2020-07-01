package org.openmrs.module.gpconnect.providers;

import lombok.AccessLevel;
import lombok.Getter;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GPConnectPatientProvider, Patient> {
    @Override
    public String getServletName() {
        return "gpconnect/gpconnectServlet";
    }

    private static final String PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";

    @Mock
    FhirPatientService patientService;

    @Mock
    NhsPatientMapper nhsPatientMapper;

    @Getter(AccessLevel.PUBLIC)
    @InjectMocks
    private GPConnectPatientProvider resourceProvider;

    @Test
    public void shouldGetPatientByUuid() throws Exception {
        org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
        when(patientService.get(PATIENT_UUID)).thenReturn(patient);

        Patient r3Patient = new Patient();
        r3Patient.setId(PATIENT_UUID);
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();

        assertThat(response, isOk());
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        Patient resource = readResponse(response);
        assertThat(resource.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
    }
}

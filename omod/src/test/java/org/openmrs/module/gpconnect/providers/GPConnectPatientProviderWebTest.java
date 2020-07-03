package org.openmrs.module.gpconnect.providers;

import ca.uhn.fhir.rest.param.TokenAndListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.io.IOUtils;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.module.fhir2.api.FhirPatientService;
import org.openmrs.module.fhir2.api.dao.FhirPatientDao;
import org.openmrs.module.fhir2.api.search.param.SearchParameterMap;
import org.openmrs.module.gpconnect.mappers.NhsPatientMapper;
import org.openmrs.module.gpconnect.util.Extensions;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GPConnectPatientProviderWebTest extends BaseFhirR3ResourceProviderWebTest<GPConnectPatientProvider, Patient> {

    private static final String PATIENT_UUID = "0b42f99b-776e-4388-8f6f-84357ae2a8fb";

    @Mock
    FhirPatientService patientService;

    @Mock
    NhsPatientMapper nhsPatientMapper;

    @Mock
    FhirPatientDao patientDao;

    @Getter(AccessLevel.PUBLIC)
    @InjectMocks
    private GPConnectPatientProvider resourceProvider;

    @Captor
    private ArgumentCaptor<SearchParameterMap> searchParameterMapArgumentCaptor;

    @Test
    public void shouldGetPatientByUuid() throws Exception {
        org.hl7.fhir.r4.model.Patient patient = new org.hl7.fhir.r4.model.Patient();
        when(patientService.get(PATIENT_UUID)).thenReturn(patient);

        Patient r3Patient = new Patient();
        r3Patient.setId(PATIENT_UUID);
        when(nhsPatientMapper.enhance(Matchers.any())).thenReturn(r3Patient);

        MockHttpServletResponse response = get("/Patient/" + PATIENT_UUID).accept(FhirMediaTypes.JSON).go();

        verify(nhsPatientMapper, atLeastOnce()).enhance(Matchers.any());

        assertThat(response, isOk());
        assertThat(response.getContentType(), equalTo(FhirMediaTypes.JSON.toString()));

        Patient resource = readResponse(response);
        assertThat(resource.getIdElement().getIdPart(), equalTo(PATIENT_UUID));
    }

    @Test
    public void shouldReturn409WhenNhsNumberExists() throws IOException, ServletException {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream("patientRegister.json");
         String patientRegisterTemplate =  IOUtils.toString(is, StandardCharsets.UTF_8.name());

        String nhsNumber = "1234567890";

        String patientRegister = patientRegisterTemplate.replace("$$nhsNumber$$", nhsNumber);

        when(patientDao.search(Matchers.any(), Matchers.any()))
                .thenReturn(Collections.singletonList(new org.openmrs.Patient()));

        MockHttpServletResponse response = post("/Patient/$gpc.registerpatient").jsonContent(patientRegister).go();

        assertThat(response, statusEquals(409));

        verify(patientDao, times(1)).search(searchParameterMapArgumentCaptor.capture(), any());
        verify(patientService, never()).create(any());
        TokenParam tokenParam = ((TokenAndListParam) searchParameterMapArgumentCaptor.getValue().getParameters("identifier.search.handler").get(0).getParam()).getValuesAsQueryTokens().get(0).getValuesAsQueryTokens().get(0);
        assertThat(tokenParam.getSystem(), equalTo(Extensions.NHS_NUMBER_SYSTEM));
        assertThat(tokenParam.getValue(), equalTo(nhsNumber));

    }
}

package org.openmrs.module.gpconnect.mappers;

import org.hl7.fhir.dstu3.model.Extension;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Period;
import org.hl7.fhir.dstu3.model.Reference;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.gpconnect.entity.NhsPatient;
import org.openmrs.module.gpconnect.util.Extensions;

import java.util.Date;
import java.util.Optional;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RegistrationDetailsMapperTest {

    public static final NhsPatient EMPTY_NHS_PATIENT = new NhsPatient();
    CodeableConceptExtension mockCodeableConcept = mock(CodeableConceptExtension.class);

    RegistrationDetailsMapper mapper = new RegistrationDetailsMapper(mockCodeableConcept);

    Patient patient = new Patient();

    @Before
    public void setUp(){
        when(mockCodeableConcept.getValue(any(Patient.class))).thenReturn(Optional.empty());
        when(mockCodeableConcept.getValue(any(Extension.class))).thenReturn(Optional.empty());
        when(mockCodeableConcept.createExtension(any(String.class))).thenReturn(Optional.empty());
    }

    @Test
    public void shouldEnhanceWithRegistrationPeriod() {
        Date start = new DateTime(2010, 1, 1, 12, 0).toDate();
        Date end = new DateTime(2015, 1, 1, 12, 0).toDate();

        testForPeriod(start, end);
    }

    @Test
    public void shouldSkipStartWhenNull() {
        Date end = new DateTime(2015, 1, 1, 12, 0).toDate();

        testForPeriod(null, end);
    }

    @Test
    public void shouldSkipEndWhenNull() {
        Date start = new DateTime(2010, 1, 1, 12, 0).toDate();

        testForPeriod(start, null);
    }

    private void testForPeriod(Date start, Date end) {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setRegistrationStart(start);
        nhsPatient.setRegistrationEnd(end);

        Patient enhancedPatient = mapper.enhance(patient, nhsPatient);

        Extension registrationDetailsExt = enhancedPatient.getExtensionsByUrl(Extensions.REGISTRATION_DETAILS_URL).get(0);
        Extension registrationPeriodExt = registrationDetailsExt.getExtensionsByUrl(Extensions.REGISTRATION_PERIOD).get(0);

        Date mappedStart = ((Period) registrationPeriodExt.getValue()).getStart();
        Date mappedEnd = ((Period) registrationPeriodExt.getValue()).getEnd();

        assertEquals(start, mappedStart);
        assertEquals(end, mappedEnd);
    }

    @Test
    public void shouldEnhanceWithType() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setRegistrationType("R");

        Extension extension = new Extension(Extensions.REGISTRATION_TYPE);
        when(mockCodeableConcept.createExtension("R")).thenReturn(Optional.of(extension));

        Patient enhancedPatient = mapper.enhance(patient, nhsPatient);

        Extension registrationDetailsExt = enhancedPatient.getExtensionsByUrl(Extensions.REGISTRATION_DETAILS_URL).get(0);
        Extension registrationTypeExt = registrationDetailsExt.getExtensionsByUrl(Extensions.REGISTRATION_TYPE).get(0);

        assertEquals(extension, registrationTypeExt);
    }

    @Test
    public void shouldSkipTypeWhenUnknown() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setRegistrationType("something else");

        when(mockCodeableConcept.createExtension("something else")).thenReturn(Optional.empty());
        Patient enhancedPatient = mapper.enhance(patient, nhsPatient);

        Extension registrationDetailsExt = enhancedPatient.getExtensionsByUrl(Extensions.REGISTRATION_DETAILS_URL).get(0);

        assertEquals(0, registrationDetailsExt.getExtensionsByUrl(Extensions.REGISTRATION_TYPE).size());
    }

    @Test
    public void shouldSkipTypeWhenMissing() {
        NhsPatient nhsPatient = new NhsPatient();

        when(mockCodeableConcept.createExtension(null)).thenReturn(Optional.empty());
        Patient enhancedPatient = mapper.enhance(patient, nhsPatient);

        Extension registrationDetailsExt = enhancedPatient.getExtensionsByUrl(Extensions.REGISTRATION_DETAILS_URL).get(0);

        assertEquals(0, registrationDetailsExt.getExtensionsByUrl(Extensions.REGISTRATION_PERIOD).size());
        assertEquals(0, registrationDetailsExt.getExtensionsByUrl(Extensions.REGISTRATION_TYPE).size());
        assertEquals(0, registrationDetailsExt.getExtensionsByUrl(Extensions.PREFERRED_BRANCH).size());
    }

    @Test
    public void shouldEnhanceWithPreferredBranch() {
        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setPreferredBranch("some/organisation");

        Patient enhancedPatient = mapper.enhance(patient, nhsPatient);

        Extension registrationDetailsExt = enhancedPatient.getExtensionsByUrl(Extensions.REGISTRATION_DETAILS_URL).get(0);
        Extension preferredBranchExt = registrationDetailsExt.getExtensionsByUrl(Extensions.PREFERRED_BRANCH).get(0);

        assertEquals("some/organisation", ((Reference)preferredBranchExt.getValue()).getReference());
    }

    @Test
    public void shouldMapPeriod() {
        Patient patient = new Patient();

        Extension ext = new Extension(Extensions.REGISTRATION_DETAILS_URL);

        Date start = new DateTime(2010, 1, 1, 12, 0).toDate();
        Date end = new DateTime(2015, 1, 1, 12, 0).toDate();
        Period period = new Period();
        period.setStart(start);
        period.setEnd(end);
        Extension periodExt = new Extension(Extensions.REGISTRATION_PERIOD, period);
        ext.addExtension(periodExt);

        patient.addExtension(ext);

        NhsPatient nhsPatient = mapper.mapToNhsPatient(patient, new NhsPatient());

        NhsPatient expectedNhsPatient = new NhsPatient();
        expectedNhsPatient.setRegistrationStart(start);
        expectedNhsPatient.setRegistrationEnd(end);

        assertEquals(expectedNhsPatient, nhsPatient);
    }

    @Test
    public void shouldMapType() {
        Patient patient = new Patient();

        Extension ext = new Extension(Extensions.REGISTRATION_DETAILS_URL);
        Extension typeExt = new Extension(Extensions.REGISTRATION_TYPE);
        ext.addExtension(typeExt);

        patient.addExtension(ext);

        when(mockCodeableConcept.getValue(ext)).thenReturn(Optional.of("T"));

        NhsPatient nhsPatient = mapper.mapToNhsPatient(patient, new NhsPatient());

        NhsPatient expectedNhsPatient = new NhsPatient();
        expectedNhsPatient.setRegistrationType("T");

        assertEquals(expectedNhsPatient, nhsPatient);
    }

    @Test
    public void shouldSkipMapTypeWhenSystemUnknown() {
        Patient patient = new Patient();

        Extension ext = new Extension(Extensions.REGISTRATION_DETAILS_URL);
        Extension typeExt = new Extension(Extensions.REGISTRATION_TYPE);
        ext.addExtension(typeExt);

        patient.addExtension(ext);

        when(mockCodeableConcept.getValue(ext)).thenReturn(Optional.empty());

        NhsPatient nhsPatient = mapper.mapToNhsPatient(patient, new NhsPatient());

        assertEquals(EMPTY_NHS_PATIENT, nhsPatient);
    }

    @Test
    public void shouldMapPreferredBranch() {
        Patient patient = new Patient();
        Extension ext = new Extension(Extensions.REGISTRATION_DETAILS_URL);

        ext.addExtension(new Extension(Extensions.PREFERRED_BRANCH, new Reference("organisation/1")));
        patient.addExtension(ext);

        NhsPatient actualNhsPatient = mapper.mapToNhsPatient(patient, new NhsPatient());

        NhsPatient nhsPatient = new NhsPatient();
        nhsPatient.setPreferredBranch("organisation/1");

        assertEquals(nhsPatient, actualNhsPatient);
    }

    @Test
    public void shouldSkipMappingWhenMissing() {
        assertEquals(EMPTY_NHS_PATIENT, mapper.mapToNhsPatient(new Patient(), new NhsPatient()));
    }

    @Test
    public void shouldSkipMappingWhenEmpty() {
        Patient patient = new Patient();
        patient.addExtension(new Extension(Extensions.REGISTRATION_DETAILS_URL));

        assertEquals(EMPTY_NHS_PATIENT, mapper.mapToNhsPatient(patient, new NhsPatient()));
    }

}
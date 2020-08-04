package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Test;

import static org.junit.Assert.assertNull;

public class UserTranslatorTest {

    @Test
    public void returnsNullIfUserGivenIsNull() {

        UserTranslator userTranslator = new UserTranslator();

        Practitioner practitioner = userTranslator.toFhirResource(null);

        assertNull(practitioner);
    }

}

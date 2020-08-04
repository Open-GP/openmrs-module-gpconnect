package org.openmrs.module.gpconnect.translators;

import org.hl7.fhir.r4.model.Practitioner;
import org.openmrs.User;
import org.openmrs.module.fhir2.api.translators.impl.PractitionerTranslatorUserImpl;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class UserTranslator extends PractitionerTranslatorUserImpl {

    @Override
    public Practitioner toFhirResource(User user) {
        if(user == null){
            return null;
        }
        return super.toFhirResource(user);
    }
}

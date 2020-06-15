package org.openmrs.module.gpconnect.strategy;

import org.hl7.fhir.dstu3.model.Meta;
import org.hl7.fhir.dstu3.model.Practitioner;
import org.openmrs.module.fhir.api.strategies.practitioner.PractitionerStrategy;

import java.util.List;

public class GPConnectPractitionerStrategy extends PractitionerStrategy {
    @Override
    public List<Practitioner> searchPractitionersByIdentifier(String identifier) {
        List<Practitioner> practitioners = super.searchPractitionersByIdentifier(identifier);

        practitioners
                .stream()
                .map(practitioner -> practitioner.setMeta(new Meta().addProfile("https://fhir.nhs.uk/STU3/StructureDefinition/CareConnect-GPC-Practitioner-1")));
        return practitioners;
    }
}

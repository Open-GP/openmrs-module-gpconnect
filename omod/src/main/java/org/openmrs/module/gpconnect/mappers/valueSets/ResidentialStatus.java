package org.openmrs.module.gpconnect.mappers.valueSets;

import lombok.Getter;
import org.hl7.fhir.dstu3.model.Coding;
import org.openmrs.module.gpconnect.util.CodeSystems;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum ResidentialStatus {
    H("UK Resident"),
    O("Overseas Resident");

    @Getter
    private final String display;

    ResidentialStatus(String display) {
        this.display = display;
    }

    public Coding getCoding() {
        return new Coding(CodeSystems.RESIDENTIAL_STATUS, name(), display);
    }

    public static List<String> names(){
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }

    public static Map<String, String> dict(){
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        Enum::name,
                        ResidentialStatus::getDisplay
                ));
    }

}

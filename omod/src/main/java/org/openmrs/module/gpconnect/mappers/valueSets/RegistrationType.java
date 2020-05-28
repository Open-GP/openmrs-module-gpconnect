package org.openmrs.module.gpconnect.mappers.valueSets;

import lombok.Getter;
import org.hl7.fhir.dstu3.model.Coding;
import org.openmrs.module.gpconnect.util.CodeSystems;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public enum RegistrationType {
    R("Regular"),
    E("Emergency"),
    IN("Immediately necessary"),
    O("Other"),
    T("Temporary"),
    S("Synthetic Record"),
    P("Private");

    @Getter
    private final String display;

    RegistrationType(String display) {
        this.display = display;
    }

    public static Map<String, String> dict(){
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        Enum::name,
                        RegistrationType::getDisplay
                ));
    }

    public Coding getCoding() {
        return new Coding(CodeSystems.REGISTRATION_TYPE, name(), display);
    }

    public static List<String> names(){
        return Arrays.stream(values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
}

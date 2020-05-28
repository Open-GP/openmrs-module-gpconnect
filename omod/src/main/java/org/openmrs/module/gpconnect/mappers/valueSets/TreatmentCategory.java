package org.openmrs.module.gpconnect.mappers.valueSets;

import lombok.Getter;
import org.hl7.fhir.dstu3.model.Coding;
import org.openmrs.module.gpconnect.util.CodeSystems;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public enum TreatmentCategory {
    ONE("1", "Exempt from payment - subject to reciprocal health agreement"),
    TWO("2","Exempt from payment - other"),
    THREE("3","To pay hotel fees only"),
    FOUR("4","To pay all fees"),
    EIGHT("8",	"Not applicable"),
    NINE("9",	"Charging rate not known");

    @Getter
    private final String code;

    @Getter
    private final String display;

    TreatmentCategory(String code, String display) {
        this.code = code;
        this.display = display;
    }

    public static Map<String, String> dict(){
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        TreatmentCategory::getCode,
                        TreatmentCategory::getDisplay
                ));
    }


    public Coding getCoding() {
        return new Coding(CodeSystems.TREATMENT_CATEGORY, code, display);
    }

    public static boolean isValid(String code){
        return Arrays.stream(values())
                .map(TreatmentCategory::getCode)
                .collect(Collectors.toList())
                .contains(code);
    }

    public static Optional<TreatmentCategory> findByCode(String code) {
        return Arrays.stream(values())
                .filter(treatmentCategory -> treatmentCategory.getCode().equals(code))
                .findFirst();
    }
}

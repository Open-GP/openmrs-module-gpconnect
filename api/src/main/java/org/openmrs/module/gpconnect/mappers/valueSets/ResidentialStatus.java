package org.openmrs.module.gpconnect.mappers.valueSets;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ResidentialStatus {
	H("UK Resident"), O("Overseas Resident");
	
	@Getter
	private final String display;
	
	ResidentialStatus(String display) {
		this.display = display;
	}
	
	public static Map<String, String> dict(){
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        Enum::name,
                        ResidentialStatus::getDisplay
                ));
    }
}

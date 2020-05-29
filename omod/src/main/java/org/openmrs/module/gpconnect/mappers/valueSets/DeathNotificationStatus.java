package org.openmrs.module.gpconnect.mappers.valueSets;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum DeathNotificationStatus {
	ONE("1", "Informal - death notice received via an update from a local NHS Organisation such as GP or Trust"), TWO("2",
	        "Formal - death notice received from Registrar of Deaths");
	
	@Getter
	private final String code;
	
	@Getter
	private final String display;
	
	DeathNotificationStatus(String code, String display) {
		this.code = code;
		this.display = display;
	}
	
	public static Map<String, String> dict(){
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        DeathNotificationStatus::getCode,
                        DeathNotificationStatus::getDisplay
                ));
    }
}

package org.openmrs.module.gpconnect.mappers.valueSets;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum NhsNoVerificationStatus {
	
	ONE("01", "Number present and verified"), TWO("02", "Number present but not traced"), THREE("03", "Trace required"), FOUR(
	        "04", "Trace attempted - No match or multiple match found"), FIVE("05",
	        "Trace needs to be resolved - (NHS number or patient detail conflict)"), SIX("06", "Trace in progress"), SEVEN(
	        "07", "Number not present and trace not required"), EIGHT("08", "Trace postponed (baby under six weeks old)");
	
	@Getter
	private final String code;
	
	@Getter
	private final String display;
	
	NhsNoVerificationStatus(String code, String display) {
		this.code = code;
		this.display = display;
	}
	
	public static Map<String, String> dict(){
        return Arrays.stream(values())
                .collect(Collectors.toMap(
                        NhsNoVerificationStatus::getCode,
                        NhsNoVerificationStatus::getDisplay
                ));
    }
}

package org.openmrs.module.gpconnect.services;

import java.util.stream.Stream;

public class NhsNumberValidator {

    public static final String DIGITS = "1234567890";

    public static boolean validate(String nhsNumber) {
        boolean allDigits = Stream.of(nhsNumber.toCharArray())
                .allMatch(digit -> DIGITS.contains(String.valueOf(digit)));

        return nhsNumber.length() == 10 && allDigits;
    }
}


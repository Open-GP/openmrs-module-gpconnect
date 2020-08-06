package org.openmrs.module.gpconnect.services;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NhsNumberValidator {

    public static final String DIGITS = "1234567890";
    private static final int NHS_NUMBER_LENGTH = 10;
    private static final int Modulo11 = 11;

    public static boolean validate(String nhsNumber) {

        return isValidLength(nhsNumber) && validateDigitsAndCheckDigit(nhsNumber);
    }

    private static boolean isValidLength(String nhsNumber) {
        return nhsNumber.length() == NHS_NUMBER_LENGTH;
    }

    public static boolean isMatchingCheckDigit(List<Integer> nhsNumberList){
        List<Integer> validNumbers = Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
        int checkDigit = calculateCheckDigit(nhsNumberList);
        return validNumbers.contains(checkDigit) && checkDigit == nhsNumberList.get(9);
    }

    private static int calculateCheckDigit(List<Integer> digits)
    {
        int sum = 0;
        for(int index = 1; index <= 9; index++)
        {
            final int scalar = Modulo11 - index;
            final int scaledDigit = scalar * digits.get(index - 1);
            sum += scaledDigit;
        }

        final int remainder = sum % Modulo11;
        if (remainder == 0) {
            return 0;
        };
        return Modulo11 - remainder;
    }

    private static boolean validateDigitsAndCheckDigit(String nhsNumber) {
        return areDigitsValid(nhsNumber) && checkDigitMatchesLastNumber(nhsNumber);
    }

    private static boolean checkDigitMatchesLastNumber(String nhsNumber) {
        List<Integer> allDigits = Arrays.stream(nhsNumber.split(""))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        return isMatchingCheckDigit(allDigits);
    }

    private static boolean areDigitsValid(String nhsNumber) {
        return nhsNumber.codePoints()
                    .mapToObj(c -> String.valueOf((char) c))
                    .allMatch(DIGITS::contains);
    }

}


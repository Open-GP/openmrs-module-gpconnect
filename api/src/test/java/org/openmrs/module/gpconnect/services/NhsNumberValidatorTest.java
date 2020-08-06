package org.openmrs.module.gpconnect.services;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NhsNumberValidatorTest {

    @Test
    public void shouldShowAsInvalidStringsWithLengthDifferentThan10() {
        assertFalse(NhsNumberValidator.validate("abcd"));
    }

    @Test
    public void shouldShowAsInvalidStringsWithLengthOf10AndNonDigits() {
        assertFalse(NhsNumberValidator.validate("123456789a"));
    }

    @Test
    public void shouldShowAsInvalidStringsWithLengthOf10AndWrongCheckDigit() {
        assertFalse(NhsNumberValidator.validate("9000000000"));
    }

    @Test
    public void shouldShowAsValidStringsWithLengthOf10AndCorrectCheckDigit() {
        List<String> validNhsNumber = Arrays.asList("9658218865", "9658218873", "9658218881", "9658218903", "9658218997");

        validNhsNumber.forEach( number -> {
            assertTrue(NhsNumberValidator.validate(number));
        });
    }

}


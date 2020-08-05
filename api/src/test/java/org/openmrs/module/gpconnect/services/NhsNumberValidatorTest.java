package org.openmrs.module.gpconnect.services;

import org.junit.Test;

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
    public void shouldShowAsValidStringsWithLengthOf10() {
        assertTrue(NhsNumberValidator.validate("1234567890"));
    }

}

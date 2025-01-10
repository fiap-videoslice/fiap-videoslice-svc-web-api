package com.example.fiap.videosliceapi.domain.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class StringUtilsTest {

    @Test
    void testIsEmptyStringNull() {
        assertTrue(StringUtils.isEmpty(null), "Expected true when input string is null");
    }

    @Test
    void testIsEmptyStringEmpty() {
        assertTrue(StringUtils.isEmpty(""), "Expected true when input string is empty");
    }

    @Test
    void testIsEmptyStringOnlySpaces() {
        assertTrue(StringUtils.isEmpty("    "), "Expected true when input string is only spaces");
    }

    @Test
    void testIsEmptyStringNotEmpty() {
        assertFalse(StringUtils.isEmpty("Test"), "Expected false when input string is not empty");
    }
}
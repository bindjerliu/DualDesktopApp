package com.omel.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationTest {
    @Test
    void emails() {
        assertTrue(Validation.isValidEmail("student@rit.edu"));
        assertTrue(Validation.isValidEmail("  first.last+tag@mail.example.com "));
        assertFalse(Validation.isValidEmail("a"));
        assertFalse(Validation.isValidEmail("a@b"));
        assertFalse(Validation.isValidEmail("a b@c.de"));
        assertFalse(Validation.isValidEmail("a|b@c.de"));
        assertFalse(Validation.isValidEmail(""));
        assertFalse(Validation.isValidEmail(null));
    }

    @Test
    void names() {
        assertTrue(Validation.isValidName("Ann One"));
        assertFalse(Validation.isValidName("   "));
        assertFalse(Validation.isValidName(null));
        assertFalse(Validation.isValidName("x".repeat(Validation.MAX_NAME_LENGTH + 1)));
    }

    @Test
    void notes() {
        assertTrue(Validation.isNotesLengthOk(null));
        assertTrue(Validation.isNotesLengthOk("no onions"));
        assertFalse(Validation.isNotesLengthOk("x".repeat(Validation.MAX_NOTES_LENGTH + 1)));
    }
}

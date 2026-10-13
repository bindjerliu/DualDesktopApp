package com.omel.service;

import java.util.regex.Pattern;

public final class Validation {
    public static final int MAX_NAME_LENGTH = 80;
    public static final int MAX_NOTES_LENGTH = 300;

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s|]+@[^@\\s|]+\\.[^@\\s|]+$");

    private Validation() { }

    public static boolean isValidName(String name) {
        return name != null && !name.isBlank() && name.trim().length() <= MAX_NAME_LENGTH;
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.trim().length() <= 120 && EMAIL.matcher(email.trim()).matches();
    }

    public static boolean isNotesLengthOk(String notes) {
        return notes == null || notes.length() <= MAX_NOTES_LENGTH;
    }
}

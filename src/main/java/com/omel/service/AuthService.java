package com.omel.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Admin login check. Credentials come from the environment (OMEL_ADMIN_USER / OMEL_ADMIN_PASSWORD);
 * if they are not set the well-known demo login is used and {@link #isDemoCredentials()} is true.
 * This is demo-grade protection for a desktop prototype, not a real authentication system.
 */
public final class AuthService {
    public static final String ENV_USER = "OMEL_ADMIN_USER";
    public static final String ENV_PASSWORD = "OMEL_ADMIN_PASSWORD";
    static final String DEMO_USER = "admin";
    static final String DEMO_PASSWORD = "omel2026";

    private final byte[] userDigest;
    private final byte[] passwordDigest;
    private final boolean demo;

    public AuthService(String user, String password) {
        this(user, password, false);
    }

    private AuthService(String user, String password, boolean demo) {
        this.userDigest = sha256(user);
        this.passwordDigest = sha256(password);
        this.demo = demo;
    }

    public static AuthService fromEnvironment(Map<String, String> env) {
        String user = env.get(ENV_USER);
        String password = env.get(ENV_PASSWORD);
        if (user != null && !user.isBlank() && password != null && !password.isEmpty()) {
            return new AuthService(user, password, false);
        }
        return new AuthService(DEMO_USER, DEMO_PASSWORD, true);
    }

    public boolean isDemoCredentials() {
        return demo;
    }

    public boolean authenticate(String user, String password) {
        if (user == null || password == null) return false;
        // Non-short-circuit '&' and constant-time digest comparison: no early exit on the first wrong value.
        return MessageDigest.isEqual(userDigest, sha256(user)) & MessageDigest.isEqual(passwordDigest, sha256(password));
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available on the JVM", e);
        }
    }
}

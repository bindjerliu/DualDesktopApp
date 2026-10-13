package com.omel.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {
    @Test
    void acceptsOnlyTheExactCredentials() {
        AuthService auth = new AuthService("chef", "s3cret!");
        assertTrue(auth.authenticate("chef", "s3cret!"));
        assertFalse(auth.authenticate("chef", "S3cret!"));
        assertFalse(auth.authenticate("Chef", "s3cret!"));
        assertFalse(auth.authenticate("", ""));
        assertFalse(auth.authenticate(null, "s3cret!"));
        assertFalse(auth.authenticate("chef", null));
    }

    @Test
    void environmentVariablesOverrideTheDemoLogin() {
        AuthService auth = AuthService.fromEnvironment(Map.of(
                AuthService.ENV_USER, "owner", AuthService.ENV_PASSWORD, "pw-123"));
        assertFalse(auth.isDemoCredentials());
        assertTrue(auth.authenticate("owner", "pw-123"));
        assertFalse(auth.authenticate("admin", "omel2026"));
    }

    @Test
    void withoutEnvironmentVariablesTheDemoLoginIsUsedAndFlagged() {
        AuthService auth = AuthService.fromEnvironment(Map.of());
        assertTrue(auth.isDemoCredentials());
        assertTrue(auth.authenticate("admin", "omel2026"));
    }

    @Test
    void aHalfConfiguredEnvironmentFallsBackToDemo() {
        assertTrue(AuthService.fromEnvironment(Map.of(AuthService.ENV_USER, "owner")).isDemoCredentials());
        assertTrue(AuthService.fromEnvironment(Map.of(AuthService.ENV_PASSWORD, "pw")).isDemoCredentials());
    }
}

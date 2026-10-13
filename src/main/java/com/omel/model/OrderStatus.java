package com.omel.model;

import java.util.Optional;

public enum OrderStatus {
    PENDING, PREPARING, READY, COMPLETED, CANCELLED;

    /** Orders the kitchen still has to deal with. */
    public boolean isActive() {
        return this == PENDING || this == PREPARING || this == READY;
    }

    public static Optional<OrderStatus> parse(String value) {
        if (value == null) return Optional.empty();
        try {
            return Optional.of(valueOf(value.trim().toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}

package com.omel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Money is always a {@link BigDecimal}; this class is the only place it is turned into text for display. */
public final class Money {
    private Money() { }

    /** Locale-independent: always "€5.70", whatever the computer's regional settings are. */
    public static String format(BigDecimal amount) {
        return "€" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}

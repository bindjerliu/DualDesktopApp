package com.omel.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    private static Order sample(String notes, BigDecimal total) {
        return new Order("OMEL-1001", "2026-04-28 09:15", "Sample Student", "sample@rit.edu", "12:30",
                "Morning Brew Coffee x1, Chicken Caesar Wrap x1", total, "PENDING", notes);
    }

    @Test
    void roundTripKeepsEveryField() {
        Order back = Order.fromLine(sample("No onions", new BigDecimal("5.70")).serialize());
        assertEquals("OMEL-1001", back.getId());
        assertEquals("Sample Student", back.getCustomerName());
        assertEquals("sample@rit.edu", back.getCustomerEmail());
        assertEquals(0, new BigDecimal("5.70").compareTo(back.getTotal()));
        assertEquals("PENDING", back.getStatus());
        assertEquals("No onions", back.getNotes());
    }

    @Test
    void pipesAndNewlinesInNotesCannotBreakTheFormat() {
        Order back = Order.fromLine(sample("a|b\nc\r\nd", new BigDecimal("1.00")).serialize());
        assertEquals("a/b c  d", back.getNotes());
    }

    /** Regression: the total used to be written with the computer's locale ("5,70" in Albanian/German). */
    @Test
    void totalIsWrittenWithADotInEveryLocale() {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            String line = sample("", new BigDecimal("5.7")).serialize();
            assertTrue(line.contains("|5.70|"), line);
            assertEquals(0, new BigDecimal("5.70").compareTo(Order.fromLine(line).getTotal()));
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void malformedRecordsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> Order.fromLine("too|few|fields"));
        assertThrows(IllegalArgumentException.class, () -> Order.fromLine("a|b|c|d|e|f|not-a-number|h|i"));
    }

    @Test
    void lineTotalIsExactDecimalMath() {
        MenuItem latte = new MenuItem("Iced Caramel Latte", "Coffee", new BigDecimal("2.80"), "", "", true);
        assertEquals(new BigDecimal("8.40"), new CartItem(latte, 3).getLineTotal());
        assertEquals("€8.40", Money.format(new CartItem(latte, 3).getLineTotal()));
    }

    @Test
    void statusParsingIsForgiving() {
        assertEquals(OrderStatus.READY, OrderStatus.parse(" ready ").orElseThrow());
        assertTrue(OrderStatus.parse("nonsense").isEmpty());
        assertTrue(OrderStatus.parse(null).isEmpty());
        assertTrue(OrderStatus.PENDING.isActive());
        assertFalse(OrderStatus.CANCELLED.isActive());
    }
}

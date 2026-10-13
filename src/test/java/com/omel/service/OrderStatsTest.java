package com.omel.service;

import com.omel.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderStatsTest {
    private static Order order(String id, String total, String status) {
        return new Order(id, "2026-04-28 09:00", "N", "n@rit.edu", "12:00", "x", new BigDecimal(total), status, "");
    }

    private final List<Order> orders = List.of(
            order("1", "2.80", "COMPLETED"),
            order("2", "1.80", "COMPLETED"),
            order("3", "9.99", "PENDING"),
            order("4", "5.00", "CANCELLED"),
            order("5", "3.00", "READY"),
            order("6", "1.00", "SOMETHING_ELSE"));

    @Test
    void revenueOnlyCountsCompletedOrdersAndIsExact() {
        assertEquals(new BigDecimal("4.60"), OrderStats.revenue(orders));
    }

    @Test
    void activeOrdersAreThoseTheKitchenStillHasToHandle() {
        assertEquals(2, OrderStats.activeCount(orders));
        assertEquals(2, OrderStats.completedCount(orders));
    }

    @Test
    void emptyListGivesZero() {
        assertEquals(BigDecimal.ZERO, OrderStats.revenue(List.of()));
    }
}

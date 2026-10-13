package com.omel.service;

import com.omel.model.Order;
import com.omel.model.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

/** Numbers shown on the admin dashboard. */
public final class OrderStats {
    private OrderStats() { }

    /** Revenue only counts orders that were actually completed (not pending, not cancelled). */
    public static BigDecimal revenue(List<Order> orders) {
        return orders.stream()
                .filter(o -> status(o) == OrderStatus.COMPLETED)
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static long activeCount(List<Order> orders) {
        return orders.stream().filter(o -> status(o) != null && status(o).isActive()).count();
    }

    public static long completedCount(List<Order> orders) {
        return orders.stream().filter(o -> status(o) == OrderStatus.COMPLETED).count();
    }

    private static OrderStatus status(Order order) {
        return OrderStatus.parse(order.getStatus()).orElse(null);
    }
}

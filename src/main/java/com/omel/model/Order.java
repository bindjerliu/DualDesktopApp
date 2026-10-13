package com.omel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Order {
    private final String id;
    private final String timestamp;
    private final String customerName;
    private final String customerEmail;
    private final String pickupTime;
    private final String items;
    private final BigDecimal total;
    private String status;
    private final String notes;

    public Order(String id, String timestamp, String customerName, String customerEmail, String pickupTime,
                 String items, BigDecimal total, String status, String notes) {
        this.id = id;
        this.timestamp = timestamp;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.pickupTime = pickupTime;
        this.items = items;
        this.total = Objects.requireNonNull(total, "total");
        this.status = status;
        this.notes = notes;
    }

    public String getId() { return id; }
    public String getTimestamp() { return timestamp; }
    public String getCustomerName() { return customerName; }
    public String getCustomerEmail() { return customerEmail; }
    public String getPickupTime() { return pickupTime; }
    public String getItems() { return items; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public String getNotes() { return notes; }
    public void setStatus(String status) { this.status = status; }

    /** One line of the text database. The total is always written with a dot, in any locale. */
    public String serialize() {
        return String.join("|", safe(id), safe(timestamp), safe(customerName), safe(customerEmail),
                safe(pickupTime), safe(items), total.setScale(2, RoundingMode.HALF_UP).toPlainString(),
                safe(status), safe(notes));
    }

    public static Order fromLine(String line) {
        String[] p = line.split("\\|", -1);
        if (p.length != 9) throw new IllegalArgumentException("Invalid order record (expected 9 fields, got " + p.length + ")");
        try {
            return new Order(p[0], p[1], p[2], p[3], p[4], p[5], new BigDecimal(p[6]), p[7], p[8]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid order record (bad total '" + p[6] + "')", e);
        }
    }

    /** Keeps free text from breaking the one-line, pipe-separated format. */
    private static String safe(String value) {
        return value == null ? "" : value.replace("|", "/").replace("\r", " ").replace("\n", " ").trim();
    }
}

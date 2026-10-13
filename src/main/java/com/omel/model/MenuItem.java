package com.omel.model;

import java.math.BigDecimal;

public class MenuItem {
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final String description;
    private final String dietary;
    private boolean available;

    public MenuItem(String name, String category, BigDecimal price, String description, String dietary, boolean available) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.description = description;
        this.dietary = dietary;
        this.available = available;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public BigDecimal getPrice() { return price; }
    public String getDescription() { return description; }
    public String getDietary() { return dietary; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public String toString() {
        return name + " (" + Money.format(price) + ")";
    }
}

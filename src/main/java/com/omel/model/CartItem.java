package com.omel.model;

import java.math.BigDecimal;

public class CartItem {
    private final MenuItem menuItem;
    private int quantity;

    public CartItem(MenuItem menuItem, int quantity) {
        this.menuItem = menuItem;
        this.quantity = quantity;
    }

    public MenuItem getMenuItem() { return menuItem; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getLineTotal() { return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity)); }

    public String toOrderText() {
        return menuItem.getName() + " x" + quantity;
    }
}

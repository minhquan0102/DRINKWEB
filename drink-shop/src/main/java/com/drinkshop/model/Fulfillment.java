package com.drinkshop.model;

/** Hình thức nhận hàng. */
public enum Fulfillment {
    DINE_IN("Tại chỗ"),
    TAKEAWAY("Mang đi"),
    DELIVERY("Giao hàng");

    private final String label;

    Fulfillment(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

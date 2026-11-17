package com.drinkshop.model;

/** Kiểu giảm giá của voucher. */
public enum DiscountType {
    PERCENT("Giảm theo %"),
    FIXED("Giảm số tiền");

    private final String label;

    DiscountType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

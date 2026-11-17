package com.drinkshop.model;

/** Ca làm việc của nhân viên. */
public enum Shift {
    SANG("Sáng"),
    CHIEU("Chiều"),
    TOI("Tối"),
    FULL("Cả ngày");

    private final String label;

    Shift(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

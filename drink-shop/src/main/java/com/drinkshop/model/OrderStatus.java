package com.drinkshop.model;

/** Trạng thái đơn hàng. */
public enum OrderStatus {
    PENDING("Chờ xác nhận", "secondary"),
    CONFIRMED("Đã xác nhận", "info"),
    PREPARING("Đang pha chế", "warning"),
    READY("Sẵn sàng", "primary"),
    DELIVERING("Đang giao", "info"),
    COMPLETED("Hoàn thành", "success"),
    CANCELLED("Đã huỷ", "danger");

    private final String label;
    private final String badge;

    OrderStatus(String label, String badge) {
        this.label = label;
        this.badge = badge;
    }

    public String getLabel() {
        return label;
    }

    public String getBadge() {
        return badge;
    }
}

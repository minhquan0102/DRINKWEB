package com.drinkshop.model;

/** Phương thức thanh toán. */
public enum PaymentMethod {
    CASH("Tiền mặt"),
    MOMO("MoMo"),
    VNPAY("VNPay"),
    BANK_TRANSFER("Chuyển khoản"),
    COD("Thanh toán khi nhận");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}

package com.drinkshop.model.dto;

/**
 * Kết quả áp dụng voucher.
 */
public record VoucherResult(boolean valid, String message, Integer voucherId, java.math.BigDecimal discount) {

    public static VoucherResult ok(String message, Integer voucherId, java.math.BigDecimal discount) {
        return new VoucherResult(true, message, voucherId, discount);
    }

    public static VoucherResult error(String message) {
        return new VoucherResult(false, message, null, java.math.BigDecimal.ZERO);
    }
}

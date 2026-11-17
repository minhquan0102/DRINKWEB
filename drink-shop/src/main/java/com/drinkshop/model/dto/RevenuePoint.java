package com.drinkshop.model.dto;

import java.math.BigDecimal;

/** Một ngày trong biểu đồ doanh thu (tách theo kênh bán). */
public record RevenuePoint(String date, BigDecimal online, BigDecimal atCounter, BigDecimal total) {
}

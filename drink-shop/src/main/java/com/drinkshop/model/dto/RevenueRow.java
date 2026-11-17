package com.drinkshop.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Một dòng của view v_daily_revenue (doanh thu theo ngày & kênh bán).
 */
public record RevenueRow(LocalDate saleDate, String orderType, long totalOrders, BigDecimal revenue) {
}

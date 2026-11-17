package com.drinkshop.model.dto;

import java.math.BigDecimal;

/**
 * Một dòng của view v_top_products (món bán chạy).
 */
public record TopProductRow(Integer productId, String productName, long qtySold, BigDecimal revenue) {
}

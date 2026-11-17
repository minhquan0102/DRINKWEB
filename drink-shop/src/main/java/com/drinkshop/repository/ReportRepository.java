package com.drinkshop.repository;

import com.drinkshop.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * Truy vấn trên các VIEW báo cáo do app_ban_nuoc.sql tạo sẵn:
 * v_daily_revenue, v_top_products, v_low_stock.
 */
public interface ReportRepository extends JpaRepository<Order, Integer> {

    /** Doanh thu theo ngày & kênh bán: [sale_date, order_type, total_orders, revenue]. */
    @Query(value = """
            SELECT sale_date, order_type, total_orders, revenue
            FROM v_daily_revenue
            ORDER BY sale_date DESC
            """, nativeQuery = true)
    List<Object[]> dailyRevenue();

    /** Món bán chạy: [id, name, qty_sold, revenue]. */
    @Query(value = """
            SELECT id, name, qty_sold, revenue
            FROM v_top_products
            ORDER BY qty_sold DESC
            """, nativeQuery = true)
    List<Object[]> topProducts();

    /** Nguyên liệu sắp hết: [id, name, unit, stock_qty, min_qty]. */
    @Query(value = """
            SELECT id, name, unit, stock_qty, min_qty
            FROM v_low_stock
            ORDER BY name
            """, nativeQuery = true)
    List<Object[]> lowStock();
}

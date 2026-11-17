package com.drinkshop.service;

import com.drinkshop.model.Ingredient;
import com.drinkshop.model.dto.ChartPoint;
import com.drinkshop.model.dto.RevenuePoint;
import com.drinkshop.model.dto.RevenueRow;
import com.drinkshop.model.dto.TopProductRow;

import java.math.BigDecimal;
import java.util.List;

/** Nghiệp vụ báo cáo cho chủ quán (dựa trên các view của CSDL). */
public interface DashboardService {

    List<RevenueRow> revenueByDay();

    List<TopProductRow> topProducts();

    List<Ingredient> lowStockIngredients();

    BigDecimal todayRevenue();

    long todayOrderCount();

    long pendingOrderCount();

    long totalCustomers();

    long totalProducts();

    // ---------- dữ liệu cho biểu đồ ----------

    /** Doanh thu 14 ngày gần nhất, tách theo kênh bán. */
    List<RevenuePoint> revenueLast14Days();

    /** Số đơn theo từng trạng thái. */
    List<ChartPoint> orderStatusCounts();

    /** Số giao dịch đã thanh toán theo phương thức. */
    List<ChartPoint> paymentMethodCounts();

    /** Top 5 đồ uống bán chạy (theo số lượng). */
    List<ChartPoint> topProductCounts();
}

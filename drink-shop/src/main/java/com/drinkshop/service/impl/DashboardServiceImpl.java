package com.drinkshop.service.impl;

import com.drinkshop.model.Ingredient;
import com.drinkshop.model.OrderStatus;
import com.drinkshop.model.PaymentMethod;
import com.drinkshop.model.dto.ChartPoint;
import com.drinkshop.model.dto.RevenuePoint;
import com.drinkshop.model.dto.RevenueRow;
import com.drinkshop.model.dto.TopProductRow;
import com.drinkshop.repository.CustomerRepository;
import com.drinkshop.repository.IngredientRepository;
import com.drinkshop.repository.OrderRepository;
import com.drinkshop.repository.PaymentRepository;
import com.drinkshop.repository.ProductRepository;
import com.drinkshop.repository.ReportRepository;
import com.drinkshop.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final DateTimeFormatter DAY_LABEL = DateTimeFormatter.ofPattern("dd/MM");

    private final ReportRepository reportRepository;
    private final OrderRepository orderRepository;
    private final IngredientRepository ingredientRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;

    public DashboardServiceImpl(ReportRepository reportRepository,
                                OrderRepository orderRepository,
                                IngredientRepository ingredientRepository,
                                CustomerRepository customerRepository,
                                ProductRepository productRepository,
                                PaymentRepository paymentRepository) {
        this.reportRepository = reportRepository;
        this.orderRepository = orderRepository;
        this.ingredientRepository = ingredientRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public List<RevenueRow> revenueByDay() {
        List<RevenueRow> rows = new ArrayList<>();
        for (Object[] row : reportRepository.dailyRevenue()) {
            rows.add(new RevenueRow(
                    toLocalDate(row[0]),
                    row[1] == null ? "" : row[1].toString(),
                    toLong(row[2]),
                    toDecimal(row[3])));
        }
        return rows;
    }

    @Override
    public List<TopProductRow> topProducts() {
        List<TopProductRow> rows = new ArrayList<>();
        for (Object[] row : reportRepository.topProducts()) {
            rows.add(new TopProductRow(
                    row[0] == null ? null : Integer.valueOf((int) toLong(row[0])),
                    row[1] == null ? "" : row[1].toString(),
                    toLong(row[2]),
                    toDecimal(row[3])));
        }
        return rows;
    }

    @Override
    public List<Ingredient> lowStockIngredients() {
        return ingredientRepository.findLowStock();
    }

    @Override
    public BigDecimal todayRevenue() {
        return orderRepository.sumTotalByStatusSince(OrderStatus.COMPLETED, LocalDate.now().atStartOfDay());
    }

    @Override
    public long todayOrderCount() {
        return orderRepository.countSince(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0));
    }

    @Override
    public long pendingOrderCount() {
        return orderRepository.countByStatus(OrderStatus.PENDING);
    }

    @Override
    public long totalCustomers() {
        return customerRepository.count();
    }

    @Override
    public long totalProducts() {
        return productRepository.countByIsAvailableTrue();
    }

    // ---------------- dữ liệu biểu đồ ----------------

    @Override
    public List<RevenuePoint> revenueLast14Days() {
        Map<LocalDate, BigDecimal[]> byDay = new HashMap<>();
        for (Object[] row : reportRepository.dailyRevenue()) {
            LocalDate day = toLocalDate(row[0]);
            if (day == null) {
                continue;
            }
            String channel = row[1] == null ? "" : row[1].toString();
            BigDecimal revenue = toDecimal(row[3]);
            BigDecimal[] slots = byDay.computeIfAbsent(day, key -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            if ("ONLINE".equalsIgnoreCase(channel)) {
                slots[0] = slots[0].add(revenue);
            } else {
                slots[1] = slots[1].add(revenue);
            }
        }

        List<RevenuePoint> points = new ArrayList<>();
        LocalDate start = LocalDate.now().minusDays(13);
        for (int i = 0; i < 14; i++) {
            LocalDate day = start.plusDays(i);
            BigDecimal[] slots = byDay.getOrDefault(day, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            points.add(new RevenuePoint(day.format(DAY_LABEL), slots[0], slots[1], slots[0].add(slots[1])));
        }
        return points;
    }

    @Override
    public List<ChartPoint> orderStatusCounts() {
        List<ChartPoint> points = new ArrayList<>();
        for (OrderStatus status : OrderStatus.values()) {
            points.add(new ChartPoint(status.getLabel(), orderRepository.countByStatus(status)));
        }
        return points;
    }

    @Override
    public List<ChartPoint> paymentMethodCounts() {
        List<ChartPoint> points = new ArrayList<>();
        for (Object[] row : paymentRepository.countPaidByMethod()) {
            String code = row[0] == null ? "" : row[0].toString();
            String label;
            try {
                label = PaymentMethod.valueOf(code).getLabel();
            } catch (IllegalArgumentException ex) {
                label = code;
            }
            points.add(new ChartPoint(label, toLong(row[1])));
        }
        return points;
    }

    @Override
    public List<ChartPoint> topProductCounts() {
        List<ChartPoint> points = new ArrayList<>();
        for (TopProductRow row : topProducts()) {
            points.add(new ChartPoint(row.productName(), row.qtySold()));
            if (points.size() == 5) {
                break;
            }
        }
        return points;
    }

    // ---------- Chuyển đổi kiểu dữ liệu trả về từ native query ----------

    private static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate date) {
            return date;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof LocalDateTime dateTime) {
            return dateTime.toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return new java.sql.Date(date.getTime()).toLocalDate();
        }
        return LocalDate.parse(value.toString());
    }

    private static long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private static BigDecimal toDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }
        return new BigDecimal(value.toString());
    }
}

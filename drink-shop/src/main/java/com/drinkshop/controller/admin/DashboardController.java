package com.drinkshop.controller.admin;

import com.drinkshop.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/** Bảng điều khiển & báo cáo doanh thu cho chủ quán / quản lý. */
@Controller
@RequestMapping("/admin")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("todayRevenue", dashboardService.todayRevenue());
        model.addAttribute("todayOrders", dashboardService.todayOrderCount());
        model.addAttribute("pendingOrders", dashboardService.pendingOrderCount());
        model.addAttribute("totalCustomers", dashboardService.totalCustomers());
        model.addAttribute("totalProducts", dashboardService.totalProducts());
        model.addAttribute("revenueRows", dashboardService.revenueByDay().stream().limit(7).toList());
        model.addAttribute("topProducts", dashboardService.topProducts().stream().limit(5).toList());
        model.addAttribute("lowStock", dashboardService.lowStockIngredients());
        addChartData(model);
        return "admin/dashboard";
    }

    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("revenueRows", dashboardService.revenueByDay());
        model.addAttribute("topProducts", dashboardService.topProducts());
        model.addAttribute("lowStock", dashboardService.lowStockIngredients());
        addChartData(model);
        return "admin/reports";
    }

    /** Dữ liệu cho Chart.js (Thymeleaf chuyển thành JSON trong template). */
    private void addChartData(Model model) {
        model.addAttribute("revenueChart", dashboardService.revenueLast14Days());
        model.addAttribute("statusChart", dashboardService.orderStatusCounts());
        model.addAttribute("paymentChart", dashboardService.paymentMethodCounts());
        model.addAttribute("topProductChart", dashboardService.topProductCounts());
    }
}

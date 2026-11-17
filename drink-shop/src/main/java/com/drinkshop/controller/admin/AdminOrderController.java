package com.drinkshop.controller.admin;

import com.drinkshop.config.CurrentUser;
import com.drinkshop.model.Order;
import com.drinkshop.model.OrderStatus;
import com.drinkshop.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Xử lý đơn hàng cho nhân viên: xem, chuyển trạng thái. */
@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;
    private final CurrentUser currentUser;

    public AdminOrderController(OrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String list(@RequestParam(required = false) OrderStatus status, Model model) {
        List<Order> orders = status == null ? orderService.allOrders()
                : orderService.allOrders().stream().filter(o -> o.getStatus() == status).toList();

        model.addAttribute("orders", orders);
        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("pendingCount", orderService.countByStatus(OrderStatus.PENDING));
        model.addAttribute("preparingCount", orderService.countByStatus(OrderStatus.PREPARING));
        return "admin/orders";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Order order = orderService.getById(id);
        if (order == null) {
            return "redirect:/admin/orders";
        }
        model.addAttribute("order", order);
        model.addAttribute("payment", orderService.getPayment(order.getId()));
        model.addAttribute("nextStatuses", orderService.nextStatuses(order.getStatus()));
        model.addAttribute("adminView", true);
        return "order-detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Integer id,
                               @RequestParam OrderStatus newStatus,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        try {
            orderService.updateStatus(id, newStatus, currentUser.id(authentication));
            redirectAttributes.addFlashAttribute("success", "Đã chuyển sang: " + newStatus.getLabel());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }
}

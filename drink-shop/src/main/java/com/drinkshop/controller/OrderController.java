package com.drinkshop.controller;

import com.drinkshop.config.CurrentUser;
import com.drinkshop.model.Order;
import com.drinkshop.model.OrderStatus;
import com.drinkshop.model.User;
import com.drinkshop.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Đơn hàng của khách: danh sách, chi tiết, huỷ đơn. */
@Controller
public class OrderController {

    private final OrderService orderService;
    private final CurrentUser currentUser;

    public OrderController(OrderService orderService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @GetMapping("/orders")
    public String myOrders(Authentication authentication, Model model) {
        User user = currentUser.get(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (user.isStaff()) {
            return "redirect:/admin/orders";
        }
        model.addAttribute("orders", orderService.ordersOfCustomer(user.getId()));
        return "orders";
    }

    @GetMapping("/orders/{code}")
    public String orderDetail(@PathVariable String code, Authentication authentication, Model model) {
        Order order = orderService.getByCode(code);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }

        User user = currentUser.get(authentication);
        boolean staff = user != null && user.isStaff();
        boolean owner = user != null && order.getCustomer() != null
                && order.getCustomer().getUserId().equals(user.getId());

        if (!staff && !owner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền xem đơn này");
        }

        model.addAttribute("order", order);
        model.addAttribute("payment", orderService.getPayment(order.getId()));
        model.addAttribute("nextStatuses", staff ? orderService.nextStatuses(order.getStatus()) : List.of());
        model.addAttribute("canCancel", owner && order.getStatus() == OrderStatus.PENDING);
        return "order-detail";
    }

    @PostMapping("/orders/{id}/cancel")
    public String cancel(@PathVariable Integer id, Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        Order order = orderService.getById(id);
        User user = currentUser.get(authentication);
        if (order == null || user == null || order.getCustomer() == null
                || !order.getCustomer().getUserId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền huỷ đơn này");
        }
        try {
            orderService.updateStatus(id, OrderStatus.CANCELLED, user.getId());
            redirectAttributes.addFlashAttribute("success", "Đã huỷ đơn " + order.getOrderCode());
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/orders/" + order.getOrderCode();
    }
}

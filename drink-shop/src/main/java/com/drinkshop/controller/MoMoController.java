package com.drinkshop.controller;

import com.drinkshop.config.CurrentUser;
import com.drinkshop.model.Order;
import com.drinkshop.model.Payment;
import com.drinkshop.model.PaymentStatus;
import com.drinkshop.model.User;
import com.drinkshop.service.MoMoService;
import com.drinkshop.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Thanh toán online qua MoMo (sandbox).
 *
 * <ul>
 *   <li>{@code GET /payment/momo/{orderCode}} — tạo giao dịch và chuyển sang MoMo.</li>
 *   <li>{@code GET /payment/momo/return} — MoMo redirect trình duyệt về.</li>
 *   <li>{@code POST /payment/momo/ipn} — MoMo gọi server-to-server (JSON), trả 204.</li>
 * </ul>
 */
@Controller
@RequestMapping("/payment/momo")
public class MoMoController {

    private static final Logger log = LoggerFactory.getLogger(MoMoController.class);

    private final OrderService orderService;
    private final MoMoService moMoService;
    private final CurrentUser currentUser;

    public MoMoController(OrderService orderService, MoMoService moMoService, CurrentUser currentUser) {
        this.orderService = orderService;
        this.moMoService = moMoService;
        this.currentUser = currentUser;
    }

    /** Bắt đầu thanh toán MoMo cho một đơn hàng. */
    @GetMapping("/{orderCode}")
    public String initiate(@PathVariable String orderCode,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        Order order = requireOrder(orderCode, authentication);

        Payment payment = orderService.getPayment(order.getId());
        if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("success", "Đơn này đã được thanh toán.");
            return "redirect:/orders/" + orderCode;
        }

        if (moMoService.isSimulate()) {
            return "redirect:/payment/momo/simulator?orderCode=" + orderCode;
        }

        try {
            return "redirect:" + moMoService.createPaymentUrl(order);
        } catch (RuntimeException ex) {
            log.warn("MoMo lỗi khi tạo giao dịch cho {}: {}", orderCode, ex.getMessage());
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/orders/" + orderCode;
        }
    }

    /** MoMo redirect trình duyệt về đây sau khi thanh toán. */
    @GetMapping("/return")
    public String handleReturn(@RequestParam Map<String, String> params,
                               RedirectAttributes redirectAttributes) {
        String orderCode = params.get("orderId");
        String target = orderCode == null ? "/orders" : "/orders/" + orderCode;

        if (!moMoService.verifySignature(params)) {
            log.warn("MoMo return: chữ ký không hợp lệ cho đơn {}", orderCode);
            redirectAttributes.addFlashAttribute("error", "Chữ ký MoMo không hợp lệ — giao dịch bị từ chối.");
            return "redirect:" + target;
        }

        Order order = orderService.getByCode(orderCode);
        if (order != null) {
            Payment payment = orderService.getPayment(order.getId());
            long expected = payment == null || payment.getAmount() == null ? -1L : payment.getAmount().longValue();
            long received;
            try {
                received = Long.parseLong(params.getOrDefault("amount", "-1"));
            } catch (NumberFormatException ex) {
                received = -1L;
            }
            if (expected != received) {
                redirectAttributes.addFlashAttribute("error", "Số tiền MoMo trả về không khớp với đơn hàng.");
                return "redirect:" + target;
            }
        }

        boolean success = "0".equals(params.get("resultCode"));
        if (success) {
            orderService.markPaid(orderCode, params.get("transId"), null);
            redirectAttributes.addFlashAttribute("success",
                    "Thanh toán MoMo thành công. Mã giao dịch: " + params.get("transId"));
        } else {
            orderService.markFailed(orderCode, params.get("resultCode"));
            redirectAttributes.addFlashAttribute("error",
                    "Thanh toán MoMo không thành công: " + safeMessage(params.get("message")));
        }
        return "redirect:" + target;
    }

    /** MoMo gọi server-to-server; trả 204 No Content theo đặc tả. */
    @PostMapping("/ipn")
    public ResponseEntity<Void> ipn(@RequestBody Map<String, String> body) {
        log.info("MoMo IPN: orderId={}, resultCode={}", body.get("orderId"), body.get("resultCode"));
        if (!moMoService.verifySignature(body)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        String orderCode = body.get("orderId");
        Order order = orderCode == null ? null : orderService.getByCode(orderCode);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Payment payment = orderService.getPayment(order.getId());
        if (payment == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (payment.getStatus() == PaymentStatus.PAID) {
            return ResponseEntity.noContent().build();
        }
        if ("0".equals(body.get("resultCode"))) {
            orderService.markPaid(orderCode, body.get("transId"), null);
        } else {
            orderService.markFailed(orderCode, body.get("resultCode"));
        }
        return ResponseEntity.noContent().build();
    }

    // ---------------- trang mô phỏng (chỉ khi simulate=true) ----------------

    @GetMapping("/simulator")
    public String simulator(@RequestParam String orderCode, Authentication authentication, Model model) {
        if (!moMoService.isSimulate()) {
            return "redirect:/orders/" + orderCode;
        }
        Order order = requireOrder(orderCode, authentication);
        model.addAttribute("order", order);
        model.addAttribute("payment", orderService.getPayment(order.getId()));
        return "payment/momo-simulator";
    }

    @PostMapping("/simulator")
    public String simulateResult(@RequestParam String orderCode,
                                 @RequestParam(defaultValue = "true") boolean success,
                                 Authentication authentication) {
        if (!moMoService.isSimulate()) {
            return "redirect:/orders/" + orderCode;
        }
        Order order = requireOrder(orderCode, authentication);
        Map<String, String> data = moMoService.buildSimulatedReturn(order, success);
        StringBuilder query = new StringBuilder();
        data.forEach((key, value) -> {
            if (query.length() > 0) {
                query.append('&');
            }
            query.append(java.net.URLEncoder.encode(key, java.nio.charset.StandardCharsets.UTF_8))
                    .append('=')
                    .append(java.net.URLEncoder.encode(value == null ? "" : value,
                            java.nio.charset.StandardCharsets.UTF_8));
        });
        return "redirect:/payment/momo/return?" + query;
    }

    // ---------------- tiện ích ----------------

    private Order requireOrder(String orderCode, Authentication authentication) {
        Order order = orderService.getByCode(orderCode);
        if (order == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đơn hàng");
        }
        User user = currentUser.get(authentication);
        boolean staff = user != null && user.isStaff();
        boolean owner = user != null && order.getCustomer() != null
                && order.getCustomer().getUserId().equals(user.getId());
        if (!staff && !owner) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Bạn không có quyền thanh toán đơn này");
        }
        return order;
    }

    private String safeMessage(String message) {
        return message == null || message.isBlank() ? "giao dịch bị huỷ" : message;
    }
}

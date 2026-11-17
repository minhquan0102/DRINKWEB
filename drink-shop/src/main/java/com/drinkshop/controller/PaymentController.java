package com.drinkshop.controller;

import com.drinkshop.config.CurrentUser;
import com.drinkshop.config.VnPayProperties;
import com.drinkshop.model.Order;
import com.drinkshop.model.Payment;
import com.drinkshop.model.PaymentStatus;
import com.drinkshop.model.User;
import com.drinkshop.service.OrderService;
import com.drinkshop.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thanh toán online qua VNPay (sandbox).
 *
 * <ul>
 *   <li>{@code GET /payment/vnpay/{orderCode}} — tạo URL và chuyển sang cổng VNPay.</li>
 *   <li>{@code GET /payment/vnpay/return} — trình duyệt được VNPay chuyển về (return URL).</li>
 *   <li>{@code GET /payment/vnpay/ipn} — VNPay gọi server-to-server (IPN), trả JSON theo đặc tả.</li>
 * </ul>
 */
@Controller
@RequestMapping("/payment/vnpay")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final OrderService orderService;
    private final VnPayService vnPayService;
    private final VnPayProperties vnPayProperties;
    private final CurrentUser currentUser;

    public PaymentController(OrderService orderService,
                             VnPayService vnPayService,
                             VnPayProperties vnPayProperties,
                             CurrentUser currentUser) {
        this.orderService = orderService;
        this.vnPayService = vnPayService;
        this.vnPayProperties = vnPayProperties;
        this.currentUser = currentUser;
    }

    /** Bắt đầu thanh toán cho một đơn hàng. */
    @GetMapping("/{orderCode}")
    public String initiate(@PathVariable String orderCode,
                           HttpServletRequest request,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        Order order = requireOrder(orderCode, authentication);

        Payment payment = orderService.getPayment(order.getId());
        if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
            redirectAttributes.addFlashAttribute("success", "Đơn này đã được thanh toán.");
            return "redirect:/orders/" + orderCode;
        }

        if (vnPayService.isSimulate()) {
            return "redirect:/payment/vnpay/simulator?orderCode=" + orderCode;
        }
        return "redirect:" + vnPayService.createPaymentUrl(order, clientIp(request));
    }

    /**
     * VNPay gọi server-to-server để xác nhận kết quả giao dịch.
     * Phải trả JSON đúng mã: 00, 01, 02, 04, 97, 99.
     */
    @GetMapping("/ipn")
    @ResponseBody
    public Map<String, String> ipn(@RequestParam Map<String, String> params) {
        log.info("VNPay IPN: TxnRef={}, ResponseCode={}, TransactionStatus={}",
                params.get("vnp_TxnRef"), params.get("vnp_ResponseCode"), params.get("vnp_TransactionStatus"));

        if (!vnPayService.verifySignature(params)) {
            return ipnReply("97", "Invalid signature");
        }

        String orderCode = params.get("vnp_TxnRef");
        Order order = orderService.getByCode(orderCode);
        if (order == null) {
            return ipnReply("01", "Order not found");
        }

        Payment payment = orderService.getPayment(order.getId());
        if (payment == null) {
            return ipnReply("01", "Order not found");
        }

        long expectedAmount = payment.getAmount() == null
                ? 0L
                : payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
        if (expectedAmount != parseLong(params.get("vnp_Amount"))) {
            return ipnReply("04", "Invalid amount");
        }

        if (payment.getStatus() == PaymentStatus.PAID) {
            return ipnReply("02", "Order already confirmed");
        }

        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        if (success) {
            orderService.markPaid(orderCode, params.get("vnp_TransactionNo"), null);
        } else {
            orderService.markFailed(orderCode, params.get("vnp_ResponseCode"));
        }
        return ipnReply("00", "Confirm Success");
    }

    /** Trình duyệt được VNPay chuyển về sau khi thanh toán. */
    @GetMapping("/return")
    public String handleReturn(@RequestParam Map<String, String> params,
                               RedirectAttributes redirectAttributes) {
        String orderCode = params.get("vnp_TxnRef");
        String target = orderCode == null ? "/orders" : "/orders/" + orderCode;

        if (!vnPayService.verifySignature(params)) {
            log.warn("VNPay return: chữ ký không hợp lệ cho đơn {}", orderCode);
            redirectAttributes.addFlashAttribute("error",
                    "Chữ ký VNPay không hợp lệ — giao dịch bị từ chối.");
            return "redirect:" + target;
        }

        Order order = orderService.getByCode(orderCode);
        if (order != null) {
            Payment payment = orderService.getPayment(order.getId());
            long expectedAmount = payment == null || payment.getAmount() == null
                    ? 0L
                    : payment.getAmount().multiply(BigDecimal.valueOf(100)).longValue();
            if (expectedAmount != parseLong(params.get("vnp_Amount"))) {
                redirectAttributes.addFlashAttribute("error",
                        "Số tiền VNPay trả về không khớp với đơn hàng.");
                return "redirect:" + target;
            }
        }

        boolean success = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        if (success) {
            orderService.markPaid(orderCode, params.get("vnp_TransactionNo"), null);
            redirectAttributes.addFlashAttribute("success",
                    "Thanh toán VNPay thành công. Mã giao dịch: " + params.get("vnp_TransactionNo"));
        } else {
            orderService.markFailed(orderCode, params.get("vnp_ResponseCode"));
            redirectAttributes.addFlashAttribute("error", describeResponse(params.get("vnp_ResponseCode")));
        }
        return "redirect:" + target;
    }

    // ---------------- trang mô phỏng (chỉ khi simulate=true) ----------------

    @GetMapping("/simulator")
    public String simulator(@RequestParam String orderCode, Authentication authentication, Model model) {
        if (!vnPayService.isSimulate()) {
            return "redirect:/orders/" + orderCode;
        }
        Order order = requireOrder(orderCode, authentication);
        model.addAttribute("order", order);
        model.addAttribute("payment", orderService.getPayment(order.getId()));
        model.addAttribute("tmnCode", vnPayProperties.getTmnCode());
        return "payment/vnpay-simulator";
    }

    @PostMapping("/simulator")
    public String simulateResult(@RequestParam String orderCode,
                                 @RequestParam(defaultValue = "true") boolean success,
                                 Authentication authentication) {
        if (!vnPayService.isSimulate()) {
            return "redirect:/orders/" + orderCode;
        }
        Order order = requireOrder(orderCode, authentication);
        Map<String, String> params = vnPayService.buildSimulatedReturn(order, success);
        // Dùng đường dẫn tương đối để chế độ mô phỏng chạy được ở bất kỳ cổng/domain nào
        return "redirect:/payment/vnpay/return?" + vnPayService.buildReturnQuery(params);
    }

    // ---------------- tiện ích ----------------

    private Map<String, String> ipnReply(String code, String message) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("RspCode", code);
        body.put("Message", message);
        return body;
    }

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

    private long parseLong(String value) {
        try {
            return value == null ? -1L : Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return -1L;
        }
    }

    private String describeResponse(String code) {
        if (code == null) {
            return "Thanh toán không thành công.";
        }
        return switch (code) {
            case "24" -> "Bạn đã huỷ giao dịch thanh toán.";
            case "51" -> "Tài khoản không đủ số dư.";
            case "65" -> "Tài khoản đã vượt hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng đang bảo trì, vui lòng thử lại sau.";
            case "09" -> "Thẻ chưa đăng ký dịch vụ Internet Banking.";
            case "12" -> "Thẻ đã bị khoá.";
            default -> "Thanh toán không thành công (mã " + code + ").";
        };
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

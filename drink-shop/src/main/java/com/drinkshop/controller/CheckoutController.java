package com.drinkshop.controller;

import com.drinkshop.config.CartControllerAttributes;
import com.drinkshop.config.CurrentUser;
import com.drinkshop.model.Address;
import com.drinkshop.model.Order;
import com.drinkshop.model.User;
import com.drinkshop.model.dto.Cart;
import com.drinkshop.model.dto.CheckoutForm;
import com.drinkshop.model.dto.VoucherResult;
import com.drinkshop.service.CustomerService;
import com.drinkshop.service.OrderService;
import com.drinkshop.service.VoucherService;
import com.drinkshop.service.impl.OrderServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/** Bước thanh toán / đặt hàng. */
@Controller
public class CheckoutController {

    private final OrderService orderService;
    private final CustomerService customerService;
    private final VoucherService voucherService;
    private final CurrentUser currentUser;

    public CheckoutController(OrderService orderService,
                              CustomerService customerService,
                              VoucherService voucherService,
                              CurrentUser currentUser) {
        this.orderService = orderService;
        this.customerService = customerService;
        this.voucherService = voucherService;
        this.currentUser = currentUser;
    }

    @GetMapping("/checkout")
    public String checkoutPage(@RequestParam(required = false) String voucherCode,
                               HttpSession session,
                               Authentication authentication,
                               Model model) {
        Cart cart = cartOf(session);
        if (cart.isEmpty()) {
            return "redirect:/menu";
        }

        User user = currentUser.get(authentication);
        boolean staff = user != null && user.isStaff();

        BigDecimal subtotal = cart.getSubtotal();
        BigDecimal discount = BigDecimal.ZERO;
        String voucherMessage = null;
        boolean voucherOk = false;

        if (voucherCode != null && !voucherCode.isBlank()) {
            VoucherResult result = voucherService.validate(voucherCode, subtotal);
            voucherMessage = result.message();
            voucherOk = result.valid();
            if (result.valid()) {
                discount = result.discount();
            }
        }

        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("discount", discount);
        model.addAttribute("total", subtotal.subtract(discount));
        model.addAttribute("deliveryFee", OrderServiceImpl.DELIVERY_FEE);
        model.addAttribute("voucherCode", voucherCode);
        model.addAttribute("voucherMessage", voucherMessage);
        model.addAttribute("voucherOk", voucherOk);
        model.addAttribute("staffCheckout", staff);
        List<Address> addresses = staff || user == null
                ? List.of()
                : customerService.addressesOf(user.getId());
        model.addAttribute("addresses", addresses);

        if (!model.containsAttribute("checkoutForm")) {
            CheckoutForm form = new CheckoutForm();
            if (!addresses.isEmpty()) {
                form.setAddressId(addresses.get(0).getId());
            }
            model.addAttribute("checkoutForm", form);
        }
        return "checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@ModelAttribute("checkoutForm") CheckoutForm form,
                             HttpSession session,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        Cart cart = cartOf(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng đang trống.");
            return "redirect:/menu";
        }
        try {
            Order order = orderService.placeOrder(currentUser.id(authentication), cart, form);
            if (form.getPaymentMethod() == com.drinkshop.model.PaymentMethod.VNPAY) {
                redirectAttributes.addFlashAttribute("success",
                        "Đơn " + order.getOrderCode() + " đã tạo. Vui lòng hoàn tất thanh toán VNPay.");
                return "redirect:/payment/vnpay/" + order.getOrderCode();
            }
            if (form.getPaymentMethod() == com.drinkshop.model.PaymentMethod.MOMO) {
                redirectAttributes.addFlashAttribute("success",
                        "Đơn " + order.getOrderCode() + " đã tạo. Vui lòng hoàn tất thanh toán MoMo.");
                return "redirect:/payment/momo/" + order.getOrderCode();
            }
            redirectAttributes.addFlashAttribute("success",
                    "Đặt hàng thành công! Mã đơn: " + order.getOrderCode());
            return "redirect:/orders/" + order.getOrderCode();
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("checkoutForm", form);
            return "redirect:/checkout";
        }
    }

    @PostMapping("/checkout/address")
    public String addAddress(@RequestParam String receiverName,
                             @RequestParam String receiverPhone,
                             @RequestParam String addressLine,
                             @RequestParam(required = false) String ward,
                             @RequestParam(required = false) String district,
                             @RequestParam String city,
                             @RequestParam(defaultValue = "false") boolean isDefault,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        Integer userId = currentUser.id(authentication);
        if (userId == null) {
            return "redirect:/login";
        }
        Address address = new Address();
        address.setReceiverName(receiverName.trim());
        address.setReceiverPhone(receiverPhone.trim());
        address.setAddressLine(addressLine.trim());
        address.setWard(ward);
        address.setDistrict(district);
        address.setCity(city.trim());
        address.setDefault(isDefault);
        customerService.addAddress(userId, address);
        redirectAttributes.addFlashAttribute("success", "Đã thêm địa chỉ giao hàng.");
        return "redirect:/checkout";
    }

    private Cart cartOf(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(CartControllerAttributes.CART_KEY);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(CartControllerAttributes.CART_KEY, cart);
        }
        return cart;
    }
}

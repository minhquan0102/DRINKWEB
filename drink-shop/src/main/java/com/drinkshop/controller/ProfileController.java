package com.drinkshop.controller;

import com.drinkshop.config.CurrentUser;
import com.drinkshop.model.Customer;
import com.drinkshop.model.User;
import com.drinkshop.service.AuthService;
import com.drinkshop.service.CustomerService;
import com.drinkshop.service.OrderService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Hồ sơ cá nhân: cập nhật thông tin, đổi mật khẩu, xem địa chỉ. */
@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final AuthService authService;
    private final CustomerService customerService;
    private final OrderService orderService;
    private final CurrentUser currentUser;

    public ProfileController(AuthService authService,
                             CustomerService customerService,
                             OrderService orderService,
                             CurrentUser currentUser) {
        this.authService = authService;
        this.customerService = customerService;
        this.orderService = orderService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        User user = currentUser.get(authentication);
        if (user == null) {
            return "redirect:/login";
        }

        Customer customer = user.isStaff() ? null : customerService.getByUserId(user.getId());
        model.addAttribute("account", user);
        model.addAttribute("customer", customer);
        model.addAttribute("addresses", customer == null
                ? java.util.List.of()
                : customerService.addressesOf(user.getId()));
        model.addAttribute("orderCount", orderService.ordersOfCustomer(user.getId()).size());
        return "profile";
    }

    @PostMapping
    public String update(@RequestParam String fullName,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false)
                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate birthday,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        User user = currentUser.get(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        try {
            authService.updateProfile(user.getId(), fullName, email, birthday);
            redirectAttributes.addFlashAttribute("success", "Đã cập nhật hồ sơ.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }

    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        User user = currentUser.get(authentication);
        if (user == null) {
            return "redirect:/login";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu nhập lại không khớp.");
            return "redirect:/profile";
        }
        try {
            authService.changePassword(user.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đã đổi mật khẩu.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile";
    }
}

package com.drinkshop.controller.admin;

import com.drinkshop.model.User;
import com.drinkshop.model.UserStatus;
import com.drinkshop.model.dto.UserForm;
import com.drinkshop.service.UserAdminService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Quản trị tài khoản: danh sách, tạo, sửa, khoá/mở, đặt lại mật khẩu. */
@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserAdminService userAdminService;

    public AdminUserController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String role,
                       @RequestParam(required = false) UserStatus status,
                       Model model) {
        model.addAttribute("users", userAdminService.search(keyword, role, status));
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("roles", userAdminService.roles());
        model.addAttribute("statuses", userAdminService.statuses());
        model.addAttribute("total", userAdminService.countAll());
        model.addAttribute("customers", userAdminService.countCustomers());
        model.addAttribute("staff", userAdminService.countStaff());
        model.addAttribute("locked", userAdminService.countLocked());
        return "admin/users";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        UserForm form = new UserForm();
        form.setRoleCode("CUSTOMER");
        model.addAttribute("userForm", form);
        model.addAttribute("isNew", true);
        addLookups(model);
        return "admin/user-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        User user = userAdminService.getById(id);
        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Tài khoản không tồn tại.");
            return "redirect:/admin/users";
        }
        UserForm form = new UserForm();
        form.setFullName(user.getFullName());
        form.setPhone(user.getPhone());
        form.setEmail(user.getEmail());
        form.setRoleCode(user.getRoleCode());
        form.setStatus(user.getStatus());

        model.addAttribute("userForm", form);
        model.addAttribute("isNew", false);
        model.addAttribute("editingUser", user);
        addLookups(model);
        return "admin/user-form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("userForm") UserForm form,
                       BindingResult bindingResult,
                       @RequestParam(required = false) Integer id,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", id == null);
            if (id != null) {
                model.addAttribute("editingUser", userAdminService.getById(id));
            }
            addLookups(model);
            return "admin/user-form";
        }
        try {
            if (id == null) {
                userAdminService.create(form);
                redirectAttributes.addFlashAttribute("success", "Đã tạo tài khoản mới.");
            } else {
                userAdminService.update(id, form);
                redirectAttributes.addFlashAttribute("success", "Đã cập nhật tài khoản.");
            }
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            model.addAttribute("isNew", id == null);
            if (id != null) {
                model.addAttribute("editingUser", userAdminService.getById(id));
            }
            addLookups(model);
            return "admin/user-form";
        }
    }

    @PostMapping("/{id}/toggle")
    public String toggle(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            User user = userAdminService.toggleStatus(id);
            redirectAttributes.addFlashAttribute("success",
                    "Đã " + (user.getStatus() == UserStatus.LOCKED ? "khoá" : "mở khoá")
                            + " tài khoản " + user.getFullName() + ".");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable Integer id,
                                @RequestParam String newPassword,
                                RedirectAttributes redirectAttributes) {
        try {
            userAdminService.resetPassword(id, newPassword);
            redirectAttributes.addFlashAttribute("success", "Đã đặt lại mật khẩu.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    private void addLookups(Model model) {
        model.addAttribute("roles", userAdminService.roles());
        model.addAttribute("statuses", userAdminService.statuses());
    }
}

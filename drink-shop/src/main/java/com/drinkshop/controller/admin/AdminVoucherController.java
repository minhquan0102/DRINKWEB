package com.drinkshop.controller.admin;

import com.drinkshop.model.DiscountType;
import com.drinkshop.model.Voucher;
import com.drinkshop.service.VoucherService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Quản trị mã giảm giá. */
@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    private final VoucherService voucherService;

    public AdminVoucherController(VoucherService voucherService) {
        this.voucherService = voucherService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherService.all());
        return "admin/vouchers";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        Voucher voucher = new Voucher();
        voucher.setDiscountType(DiscountType.PERCENT);
        voucher.setDiscountValue(BigDecimal.TEN);
        voucher.setMinOrderAmount(BigDecimal.ZERO);
        voucher.setStartDate(LocalDateTime.now());
        voucher.setEndDate(LocalDateTime.now().plusMonths(1));
        model.addAttribute("voucher", voucher);
        model.addAttribute("isNew", true);
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/voucher-form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Voucher voucher = voucherService.getById(id);
        if (voucher == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy mã giảm giá.");
            return "redirect:/admin/vouchers";
        }
        model.addAttribute("voucher", voucher);
        model.addAttribute("isNew", false);
        model.addAttribute("discountTypes", DiscountType.values());
        return "admin/voucher-form";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Integer id,
                       @RequestParam String code,
                       @RequestParam(required = false) String description,
                       @RequestParam DiscountType discountType,
                       @RequestParam BigDecimal discountValue,
                       @RequestParam(required = false) BigDecimal maxDiscount,
                       @RequestParam(defaultValue = "0") BigDecimal minOrderAmount,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startDate,
                       @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endDate,
                       @RequestParam(required = false) Integer usageLimit,
                       @RequestParam(defaultValue = "false") boolean active,
                       RedirectAttributes redirectAttributes) {
        try {
            Voucher voucher = id == null ? new Voucher() : voucherService.getById(id);
            if (voucher == null) {
                throw new IllegalArgumentException("Mã giảm giá không tồn tại.");
            }
            voucher.setCode(code.trim().toUpperCase());
            voucher.setDescription(description);
            voucher.setDiscountType(discountType);
            voucher.setDiscountValue(discountValue);
            voucher.setMaxDiscount(discountType == DiscountType.PERCENT ? maxDiscount : null);
            voucher.setMinOrderAmount(minOrderAmount);
            voucher.setStartDate(startDate);
            voucher.setEndDate(endDate);
            voucher.setUsageLimit(usageLimit);
            voucher.setActive(active);
            if (id == null) {
                voucher.setUsedCount(0);
            }
            voucherService.save(voucher);
            redirectAttributes.addFlashAttribute("success", "Đã lưu mã giảm giá.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            voucherService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá mã giảm giá.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể xoá: mã đã được dùng trong đơn hàng.");
        }
        return "redirect:/admin/vouchers";
    }
}

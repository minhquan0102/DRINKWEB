package com.drinkshop.controller.admin;

import com.drinkshop.model.Ingredient;
import com.drinkshop.service.IngredientService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

/** Quản trị kho nguyên liệu. */
@Controller
@RequestMapping("/admin/ingredients")
public class AdminIngredientController {

    private final IngredientService ingredientService;

    public AdminIngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("ingredients", ingredientService.all());
        model.addAttribute("lowStock", ingredientService.lowStock());
        return "admin/ingredients";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Integer id,
                       @RequestParam String name,
                       @RequestParam String unit,
                       @RequestParam(defaultValue = "0") BigDecimal stockQty,
                       @RequestParam(defaultValue = "0") BigDecimal minQty,
                       RedirectAttributes redirectAttributes) {
        try {
            Ingredient ingredient = id == null ? new Ingredient() : ingredientService.getById(id);
            if (ingredient == null) {
                throw new IllegalArgumentException("Nguyên liệu không tồn tại.");
            }
            ingredient.setName(name.trim());
            ingredient.setUnit(unit.trim());
            ingredient.setStockQty(stockQty);
            ingredient.setMinQty(minQty);
            ingredientService.save(ingredient);
            redirectAttributes.addFlashAttribute("success", "Đã lưu nguyên liệu.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/ingredients";
    }

    @PostMapping("/{id}/restock")
    public String restock(@PathVariable Integer id,
                          @RequestParam BigDecimal quantity,
                          RedirectAttributes redirectAttributes) {
        try {
            ingredientService.restock(id, quantity);
            redirectAttributes.addFlashAttribute("success", "Đã nhập thêm hàng vào kho.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/ingredients";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            ingredientService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá nguyên liệu.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể xoá nguyên liệu này.");
        }
        return "redirect:/admin/ingredients";
    }
}

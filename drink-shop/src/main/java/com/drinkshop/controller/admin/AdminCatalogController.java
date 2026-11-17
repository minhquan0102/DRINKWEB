package com.drinkshop.controller.admin;

import com.drinkshop.model.Category;
import com.drinkshop.model.Product;
import com.drinkshop.service.ProductService;
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

/** Quản trị menu: đồ uống và danh mục. */
@Controller
@RequestMapping("/admin")
public class AdminCatalogController {

    private final ProductService productService;

    public AdminCatalogController(ProductService productService) {
        this.productService = productService;
    }

    // ---------------- ĐỒ UỐNG ----------------

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", productService.allProducts());
        model.addAttribute("categories", productService.allCategories());
        return "admin/products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.allCategories());
        model.addAttribute("isNew", true);
        return "admin/product-form";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Product product = productService.getProduct(id);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đồ uống.");
            return "redirect:/admin/products";
        }
        model.addAttribute("product", product);
        model.addAttribute("categories", productService.allCategories());
        model.addAttribute("isNew", false);
        return "admin/product-form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@RequestParam(required = false) Integer id,
                              @RequestParam String name,
                              @RequestParam(required = false) String description,
                              @RequestParam BigDecimal basePrice,
                              @RequestParam(required = false) String imageUrl,
                              @RequestParam Integer categoryId,
                              @RequestParam(defaultValue = "false") boolean available,
                              RedirectAttributes redirectAttributes) {
        try {
            Category category = productService.allCategories().stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không hợp lệ."));

            Product product = id == null ? new Product() : productService.getProduct(id);
            if (product == null) {
                throw new IllegalArgumentException("Đồ uống không tồn tại.");
            }
            product.setName(name.trim());
            product.setDescription(description);
            product.setBasePrice(basePrice);
            product.setImageUrl(imageUrl == null || imageUrl.isBlank() ? null : imageUrl.trim());
            product.setCategory(category);
            product.setAvailable(available);
            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", id == null ? "Đã thêm đồ uống." : "Đã cập nhật đồ uống.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá đồ uống.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xoá: đồ uống đã có trong đơn hàng. Hãy tắt \"còn bán\" thay vì xoá.");
        }
        return "redirect:/admin/products";
    }

    // ---------------- DANH MỤC ----------------

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", productService.allCategories());
        return "admin/categories";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@RequestParam(required = false) Integer id,
                               @RequestParam String name,
                               @RequestParam(defaultValue = "0") Integer sortOrder,
                               @RequestParam(defaultValue = "false") boolean active,
                               RedirectAttributes redirectAttributes) {
        try {
            Category category = id == null ? new Category() : productService.allCategories().stream()
                    .filter(c -> c.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if (category == null) {
                throw new IllegalArgumentException("Danh mục không tồn tại.");
            }
            if (productService.allCategories().stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name.trim())
                            && !c.getId().equals(category.getId()))) {
                throw new IllegalArgumentException("Tên danh mục đã tồn tại.");
            }
            category.setName(name.trim());
            category.setSortOrder(sortOrder);
            category.setActive(active);
            productService.saveCategory(category);
            redirectAttributes.addFlashAttribute("success", "Đã lưu danh mục.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("success", "Đã xoá danh mục.");
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể xoá: danh mục đang có đồ uống.");
        }
        return "redirect:/admin/categories";
    }
}

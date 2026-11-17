package com.drinkshop.controller;

import com.drinkshop.model.Product;
import com.drinkshop.model.Review;
import com.drinkshop.model.Voucher;
import com.drinkshop.service.ProductService;
import com.drinkshop.service.VoucherService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

/** Trang công khai: trang chủ, menu, chi tiết đồ uống. */
@Controller
public class HomeController {

    private final ProductService productService;
    private final VoucherService voucherService;

    public HomeController(ProductService productService, VoucherService voucherService) {
        this.productService = productService;
        this.voucherService = voucherService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productService.availableProducts();
        model.addAttribute("categories", productService.activeCategories());
        model.addAttribute("products", products.stream().limit(8).toList());
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("vouchers", activeVouchers().stream().limit(3).toList());
        return "index";
    }

    @GetMapping("/menu")
    public String menu(@RequestParam(required = false) Integer categoryId,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        List<Product> products;
        if (keyword != null && !keyword.isBlank()) {
            products = productService.searchProducts(keyword);
        } else {
            products = productService.productsByCategory(categoryId);
        }
        model.addAttribute("categories", productService.activeCategories());
        model.addAttribute("products", products);
        model.addAttribute("activeCategoryId", categoryId);
        model.addAttribute("keyword", keyword);
        return "menu";
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {
        Product product = productService.getProduct(id);
        if (product == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy đồ uống");
        }
        List<Review> reviews = productService.reviewsOf(id);
        model.addAttribute("product", product);
        model.addAttribute("sizes", productService.allSizes());
        model.addAttribute("toppings", productService.availableToppings());
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", productService.averageRating(id));
        return "product-detail";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "403";
    }

    private List<Voucher> activeVouchers() {
        LocalDateTime now = LocalDateTime.now();
        return voucherService.all().stream()
                .filter(Voucher::isActive)
                .filter(v -> v.getEndDate() == null || v.getEndDate().isAfter(now))
                .toList();
    }
}

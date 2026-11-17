package com.drinkshop.controller;

import com.drinkshop.config.CartControllerAttributes;
import com.drinkshop.model.dto.Cart;
import com.drinkshop.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Giỏ hàng: xem, thêm, sửa số lượng, xoá. */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Cart cart = cartOf(session);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", cart.getSubtotal());
        return "cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam Integer productId,
                      @RequestParam Integer sizeId,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false) List<Integer> toppingIds,
                      @RequestParam(defaultValue = "100%") String sugarLevel,
                      @RequestParam(defaultValue = "100%") String iceLevel,
                      @RequestParam(required = false) String note,
                      HttpSession session,
                      RedirectAttributes redirectAttributes,
                      @RequestHeader(value = "Referer", required = false) String referer) {
        try {
            cartService.addToCart(cartOf(session), productId, sizeId, quantity,
                    toppingIds, sugarLevel, iceLevel, note);
            redirectAttributes.addFlashAttribute("success", "Đã thêm vào giỏ hàng.");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        if (referer != null && referer.contains("/product/")) {
            return "redirect:" + referer.substring(referer.indexOf("/product/"));
        }
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String update(@RequestParam String lineId,
                         @RequestParam int quantity,
                         HttpSession session) {
        cartService.updateQuantity(cartOf(session), lineId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam String lineId, HttpSession session) {
        cartService.removeItem(cartOf(session), lineId);
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clear(HttpSession session) {
        cartService.clear(cartOf(session));
        return "redirect:/cart";
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

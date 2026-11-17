package com.drinkshop.config;

import com.drinkshop.model.User;
import com.drinkshop.model.dto.Cart;
import com.drinkshop.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

/**
 * Đưa các thông tin dùng chung vào mọi trang: người dùng hiện tại, số món trong giỏ, quyền.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    public GlobalModelAttributes(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @ModelAttribute
    public void addGlobalAttributes(Model model, HttpSession session, Authentication authentication,
                                    HttpServletRequest request) {
        model.addAttribute("currentPath", request.getRequestURI());
        Cart cart = (Cart) session.getAttribute(CartControllerAttributes.CART_KEY);
        model.addAttribute("cartCount", cart == null ? 0 : cart.getTotalQuantity());

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (!loggedIn) {
            model.addAttribute("currentUser", null);
            model.addAttribute("isStaff", false);
            model.addAttribute("isManager", false);
            return;
        }

        User user = userRepository.findByPhone(authentication.getName()).orElse(null);
        model.addAttribute("currentUser", user);
        if (user != null) {
            model.addAttribute("isStaff", user.isStaff());
            model.addAttribute("isManager", List.of("OWNER", "MANAGER").contains(user.getRoleCode()));
        } else {
            model.addAttribute("isStaff", false);
            model.addAttribute("isManager", false);
        }
    }
}

package com.drinkshop.config;

import com.drinkshop.model.User;
import com.drinkshop.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Tiện ích lấy người dùng đang đăng nhập từ SecurityContext. */
@Component
public class CurrentUser {

    private final UserRepository userRepository;

    public CurrentUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User get(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        return userRepository.findByPhone(authentication.getName()).orElse(null);
    }

    public Integer id(Authentication authentication) {
        User user = get(authentication);
        return user == null ? null : user.getId();
    }

    public Integer requireId(Authentication authentication) {
        User user = get(authentication);
        if (user == null) {
            throw new IllegalStateException("Bạn cần đăng nhập để thực hiện thao tác này.");
        }
        return user.getId();
    }
}

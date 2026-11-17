package com.drinkshop.config;

import com.drinkshop.model.User;
import com.drinkshop.repository.UserRepository;
import com.drinkshop.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Script app_ban_nuoc.sql chỉ chứa password_hash giả.
 * Lớp này thay các hash giả bằng BCrypt thật để tất cả tài khoản demo
 * đăng nhập được bằng mật khẩu app.demo-password (mặc định: 123456).
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** Hash BCrypt hợp lệ có dạng $2a$10$ + 53 ký tự. */
    private static final Pattern BCRYPT = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");

    private final UserRepository userRepository;
    private final AuthService authService;

    @Value("${app.demo-password:123456}")
    private String demoPassword;

    public DataInitializer(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        int updated = 0;
        for (User user : users) {
            String hash = user.getPasswordHash();
            if (hash == null || !BCRYPT.matcher(hash).matches()) {
                authService.updatePassword(user, demoPassword);
                updated++;
            }
        }
        if (updated > 0) {
            log.info("Đã khởi tạo mật khẩu demo '{}' cho {} tài khoản.", demoPassword, updated);
        }
        log.info("Tài khoản demo: 0901000001 (chủ quán) | 0901000002 (quản lý) | "
                + "0901000003 (thu ngân) | 0901000004 (pha chế) | 0912000001 (khách) - mật khẩu: {}", demoPassword);
    }
}

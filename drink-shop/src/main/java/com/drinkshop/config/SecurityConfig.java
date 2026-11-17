package com.drinkshop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * PHÂN QUYỀN
 * - Khách (chưa đăng nhập): xem menu, sản phẩm, đăng nhập / đăng ký.
 * - CUSTOMER: giỏ hàng, đặt đơn, xem đơn của mình.
 * - CASHIER / BARISTA: xử lý đơn hàng.
 * - OWNER / MANAGER: quản lý menu, voucher, kho, báo cáo.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Tài nguyên tĩnh + trang công khai
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                        .requestMatchers("/", "/menu", "/menu/**", "/product/**",
                                "/login", "/register", "/403").permitAll()
                        // VNPay / MoMo gọi IPN server-to-server (không có session) => cho phép công khai
                        .requestMatchers("/payment/vnpay/ipn", "/payment/momo/ipn").permitAll()
                        // Chỉ chủ quán / quản lý
                        .requestMatchers("/admin/reports/**", "/admin/products/**", "/admin/categories/**",
                                "/admin/vouchers/**", "/admin/ingredients/**", "/admin/users/**")
                        .hasAnyRole("OWNER", "MANAGER")
                        // Nhân viên (thu ngân, pha chế) xử lý đơn
                        .requestMatchers("/admin/orders/**").hasAnyRole("OWNER", "MANAGER", "CASHIER", "BARISTA")
                        // Còn lại phải đăng nhập
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("phone")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                // IPN là POST từ đối tác, không có CSRF token
                .csrf(csrf -> csrf.ignoringRequestMatchers("/payment/vnpay/ipn", "/payment/momo/ipn"))
                .exceptionHandling(ex -> ex.accessDeniedPage("/403"));

        return http.build();
    }
}

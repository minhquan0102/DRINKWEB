package com.drinkshop.service.impl;

import com.drinkshop.model.Customer;
import com.drinkshop.model.Role;
import com.drinkshop.model.User;
import com.drinkshop.model.UserStatus;
import com.drinkshop.model.dto.RegisterForm;
import com.drinkshop.repository.CustomerRepository;
import com.drinkshop.repository.RoleRepository;
import com.drinkshop.repository.UserRepository;
import com.drinkshop.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User register(RegisterForm form) {
        String phone = form.getPhone() == null ? "" : form.getPhone().trim();
        String email = form.getEmail() == null || form.getEmail().isBlank() ? null : form.getEmail().trim();

        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Số điện thoại này đã được đăng ký.");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email này đã được đăng ký.");
        }
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu nhập lại không khớp.");
        }

        Role customerRole = roleRepository.findByCode("CUSTOMER")
                .orElseThrow(() -> new IllegalStateException("CSDL thiếu role CUSTOMER"));

        User user = new User();
        user.setRole(customerRole);
        user.setFullName(form.getFullName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        Customer customer = new Customer(user);
        customer.setLoyaltyPoints(0);
        customerRepository.save(customer);

        return user;
    }

    @Override
    @Transactional(readOnly = true)
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone).orElse(null);
    }

    @Override
    public void updatePassword(User user, String rawPassword) {
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    @Override
    public User updateProfile(Integer userId, String fullName, String email, java.time.LocalDate birthday) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        String cleanEmail = email == null || email.isBlank() ? null : email.trim();
        if (cleanEmail != null) {
            userRepository.findAll().stream()
                    .filter(other -> !other.getId().equals(user.getId()))
                    .filter(other -> cleanEmail.equalsIgnoreCase(other.getEmail()))
                    .findFirst()
                    .ifPresent(other -> {
                        throw new IllegalArgumentException("Email đã được dùng cho tài khoản khác.");
                    });
        }
        user.setFullName(fullName.trim());
        user.setEmail(cleanEmail);
        userRepository.save(user);

        if (birthday != null) {
            customerRepository.findByUser_Id(userId).ifPresent(customer -> {
                customer.setBirthday(birthday);
                customerRepository.save(customer);
            });
        }
        return user;
    }

    @Override
    public void changePassword(Integer userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng.");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean phoneExists(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return email != null && !email.isBlank() && userRepository.existsByEmail(email.trim());
    }
}

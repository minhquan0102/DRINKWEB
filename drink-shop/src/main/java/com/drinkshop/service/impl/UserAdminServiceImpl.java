package com.drinkshop.service.impl;

import com.drinkshop.model.Customer;
import com.drinkshop.model.Role;
import com.drinkshop.model.Shift;
import com.drinkshop.model.Staff;
import com.drinkshop.model.User;
import com.drinkshop.model.UserStatus;
import com.drinkshop.model.dto.UserForm;
import com.drinkshop.repository.CustomerRepository;
import com.drinkshop.repository.RoleRepository;
import com.drinkshop.repository.StaffRepository;
import com.drinkshop.repository.UserRepository;
import com.drinkshop.service.UserAdminService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@Transactional
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAdminServiceImpl(UserRepository userRepository,
                                RoleRepository roleRepository,
                                CustomerRepository customerRepository,
                                StaffRepository staffRepository,
                                PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> search(String keyword, String roleCode, UserStatus status) {
        String needle = keyword == null ? "" : keyword.trim().toLowerCase();
        return userRepository.findAll().stream()
                .filter(u -> needle.isEmpty()
                        || u.getFullName().toLowerCase().contains(needle)
                        || u.getPhone().contains(needle)
                        || (u.getEmail() != null && u.getEmail().toLowerCase().contains(needle)))
                .filter(u -> roleCode == null || roleCode.isBlank() || roleCode.equals(u.getRoleCode()))
                .filter(u -> status == null || status == u.getStatus())
                .sorted(Comparator.comparing(User::getId).reversed())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public User getById(Integer id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> roles() {
        return roleRepository.findAll().stream()
                .sorted(Comparator.comparing(Role::getId))
                .toList();
    }

    @Override
    public UserStatus[] statuses() {
        return UserStatus.values();
    }

    @Override
    public User create(UserForm form) {
        String phone = form.getPhone().trim();
        String email = blankToNull(form.getEmail());

        if (userRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Số điện thoại đã được dùng cho tài khoản khác.");
        }
        if (email != null && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được dùng cho tài khoản khác.");
        }
        if (form.getPassword() == null || form.getPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        Role role = requireRole(form.getRoleCode());

        User user = new User();
        user.setRole(role);
        user.setFullName(form.getFullName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setStatus(form.getStatus() == null ? UserStatus.ACTIVE : form.getStatus());
        userRepository.save(user);

        ensureProfile(user, form.getPosition());
        return user;
    }

    @Override
    public User update(Integer id, UserForm form) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));

        String phone = form.getPhone().trim();
        String email = blankToNull(form.getEmail());

        userRepository.findAll().stream()
                .filter(other -> !Objects.equals(other.getId(), user.getId()))
                .filter(other -> other.getPhone().equals(phone))
                .findFirst()
                .ifPresent(other -> {
                    throw new IllegalArgumentException("Số điện thoại đã được dùng cho tài khoản khác.");
                });
        if (email != null) {
            userRepository.findAll().stream()
                    .filter(other -> !Objects.equals(other.getId(), user.getId()))
                    .filter(other -> email.equalsIgnoreCase(other.getEmail()))
                    .findFirst()
                    .ifPresent(other -> {
                        throw new IllegalArgumentException("Email đã được dùng cho tài khoản khác.");
                    });
        }

        Role role = requireRole(form.getRoleCode());
        user.setRole(role);
        user.setFullName(form.getFullName().trim());
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(form.getStatus() == null ? user.getStatus() : form.getStatus());
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            if (form.getPassword().length() < 6) {
                throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
            }
            user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        }
        userRepository.save(user);

        ensureProfile(user, form.getPosition());
        return user;
    }

    @Override
    public User toggleStatus(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        if (user.getStatus() == UserStatus.ACTIVE) {
            user.setStatus(UserStatus.LOCKED);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }
        return userRepository.save(user);
    }

    @Override
    public void resetPassword(Integer id, String rawPassword) {
        if (rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại."));
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAll() {
        return userRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> "CUSTOMER".equals(u.getRoleCode()))
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countStaff() {
        return userRepository.findAll().stream()
                .filter(u -> !"CUSTOMER".equals(u.getRoleCode()))
                .count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countLocked() {
        return userRepository.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.LOCKED)
                .count();
    }

    // ---------------- tiện ích ----------------

    private Role requireRole(String code) {
        return roleRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ: " + code));
    }

    /** Đảm bảo có bản ghi customers / staff tương ứng với vai trò. */
    private void ensureProfile(User user, String position) {
        if ("CUSTOMER".equals(user.getRoleCode())) {
            if (customerRepository.findByUser_Id(user.getId()).isEmpty()) {
                Customer customer = new Customer(user);
                customer.setLoyaltyPoints(0);
                customerRepository.save(customer);
            }
        } else {
            if (staffRepository.findByUser_Id(user.getId()).isEmpty()) {
                Staff staff = new Staff(user);
                staff.setPosition(position == null || position.isBlank()
                        ? user.getRole().getName() : position.trim());
                staff.setHireDate(LocalDate.now());
                staff.setShift(Shift.FULL);
                staff.setSalary(BigDecimal.ZERO);
                staffRepository.save(staff);
            }
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}

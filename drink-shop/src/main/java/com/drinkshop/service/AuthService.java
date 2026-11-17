package com.drinkshop.service;

import com.drinkshop.model.User;
import com.drinkshop.model.dto.RegisterForm;

/** Nghiệp vụ tài khoản: đăng ký, tra cứu người dùng. */
public interface AuthService {

    /**
     * Đăng ký tài khoản khách hàng mới (tạo cả bản ghi users và customers).
     *
     * @throws IllegalArgumentException nếu số điện thoại / email đã tồn tại hoặc mật khẩu không khớp
     */
    User register(RegisterForm form);

    User findByPhone(String phone);

    /** Gán mật khẩu đã băm (dùng cho việc tạo dữ liệu demo). */
    void updatePassword(User user, String rawPassword);

    /** Cập nhật hồ sơ cá nhân: họ tên, email, ngày sinh (nếu là khách hàng). */
    User updateProfile(Integer userId, String fullName, String email, java.time.LocalDate birthday);

    /** Đổi mật khẩu, kiểm tra mật khẩu hiện tại. */
    void changePassword(Integer userId, String currentPassword, String newPassword);

    boolean phoneExists(String phone);

    boolean emailExists(String email);
}

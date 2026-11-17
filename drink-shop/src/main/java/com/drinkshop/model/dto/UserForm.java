package com.drinkshop.model.dto;

import com.drinkshop.model.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Form tạo / sửa tài khoản trong trang quản trị. */
public class UserForm {

    @NotBlank(message = "Vui lòng nhập họ tên")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String fullName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(max = 15, message = "Số điện thoại tối đa 15 ký tự")
    private String phone;

    @Email(message = "Email không hợp lệ")
    @Size(max = 120, message = "Email tối đa 120 ký tự")
    private String email;

    @NotBlank(message = "Vui lòng chọn vai trò")
    private String roleCode;

    private UserStatus status = UserStatus.ACTIVE;

    /** Bắt buộc khi tạo mới, để trống khi sửa nghĩa là giữ nguyên mật khẩu. */
    @Size(max = 60, message = "Mật khẩu tối đa 60 ký tự")
    private String password;

    /** Chức danh cho nhân viên (không bắt buộc). */
    private String position;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}

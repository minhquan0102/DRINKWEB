package com.drinkshop.service;

import com.drinkshop.model.Role;
import com.drinkshop.model.User;
import com.drinkshop.model.UserStatus;
import com.drinkshop.model.dto.UserForm;

import java.util.List;

/** Nghiệp vụ quản trị tài khoản người dùng (nhân viên + khách hàng). */
public interface UserAdminService {

    /** Lọc theo từ khoá (tên / SĐT), vai trò và trạng thái — tham số nào null thì bỏ qua. */
    List<User> search(String keyword, String roleCode, UserStatus status);

    User getById(Integer id);

    List<Role> roles();

    UserStatus[] statuses();

    User create(UserForm form);

    User update(Integer id, UserForm form);

    /** Đổi ACTIVE ↔ LOCKED. */
    User toggleStatus(Integer id);

    void resetPassword(Integer id, String rawPassword);

    long countAll();

    long countCustomers();

    long countStaff();

    long countLocked();
}

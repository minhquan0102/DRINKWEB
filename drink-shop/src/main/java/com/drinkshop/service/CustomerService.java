package com.drinkshop.service;

import com.drinkshop.model.Address;
import com.drinkshop.model.Customer;

import java.util.List;

/** Nghiệp vụ khách hàng: hồ sơ, địa chỉ giao hàng. */
public interface CustomerService {

    Customer getByUserId(Integer userId);

    /** Tạo hồ sơ khách hàng cho user (nếu chưa có). */
    Customer ensureCustomer(Integer userId);

    List<Address> addressesOf(Integer userId);

    Address getAddress(Integer addressId);

    Address addAddress(Integer userId, Address address);
}

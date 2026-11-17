package com.drinkshop.repository;

import com.drinkshop.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByUser_Id(Integer userId);

    Optional<Customer> findByUser_Phone(String phone);
}

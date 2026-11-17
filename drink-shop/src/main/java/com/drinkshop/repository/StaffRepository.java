package com.drinkshop.repository;

import com.drinkshop.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Integer> {

    Optional<Staff> findByUser_Id(Integer userId);
}

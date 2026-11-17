package com.drinkshop.repository;

import com.drinkshop.model.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    Optional<Voucher> findByCodeIgnoreCase(String code);

    List<Voucher> findAllByOrderByCodeAsc();

    boolean existsByCodeIgnoreCase(String code);
}

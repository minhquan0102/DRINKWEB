package com.drinkshop.repository;

import com.drinkshop.model.ProductSize;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductSizeRepository extends JpaRepository<ProductSize, Integer> {

    List<ProductSize> findAllByOrderByPriceDeltaAsc();
}

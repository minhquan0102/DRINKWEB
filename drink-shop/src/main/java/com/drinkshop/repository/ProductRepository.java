package com.drinkshop.repository;

import com.drinkshop.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    List<Product> findByIsAvailableTrueOrderByNameAsc();

    List<Product> findAllByOrderByNameAsc();

    List<Product> findByIsAvailableTrueAndCategory_IdOrderByNameAsc(Integer categoryId);

    List<Product> findByCategory_IdOrderByNameAsc(Integer categoryId);

    /** Tìm theo tên (không phân biệt hoa thường). */
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :keyword, '%')) order by p.name")
    List<Product> searchByName(@Param("keyword") String keyword);

    long countByIsAvailableTrue();
}

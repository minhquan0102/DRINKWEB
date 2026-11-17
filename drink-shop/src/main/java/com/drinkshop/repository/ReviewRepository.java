package com.drinkshop.repository;

import com.drinkshop.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

    List<Review> findByProduct_IdOrderByCreatedAtDesc(Integer productId);

    List<Review> findByCustomer_UserIdOrderByCreatedAtDesc(Integer customerId);

    @Query("select coalesce(avg(r.rating), 0) from Review r where r.product.id = :productId")
    Double averageRating(@Param("productId") Integer productId);

    long countByProduct_Id(Integer productId);
}

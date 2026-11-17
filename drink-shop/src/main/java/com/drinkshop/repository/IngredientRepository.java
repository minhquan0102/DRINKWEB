package com.drinkshop.repository;

import com.drinkshop.model.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IngredientRepository extends JpaRepository<Ingredient, Integer> {

    List<Ingredient> findAllByOrderByNameAsc();

    /** Nguyên liệu có tồn kho dưới mức tối thiểu. */
    @Query("select i from Ingredient i where i.stockQty < i.minQty order by i.name")
    List<Ingredient> findLowStock();
}

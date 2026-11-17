package com.drinkshop.service;

import com.drinkshop.model.Ingredient;

import java.math.BigDecimal;
import java.util.List;

/** Nghiệp vụ kho nguyên liệu. */
public interface IngredientService {

    List<Ingredient> all();

    List<Ingredient> lowStock();

    Ingredient getById(Integer id);

    Ingredient save(Ingredient ingredient);

    /** Nhập thêm hàng vào kho. */
    Ingredient restock(Integer id, BigDecimal quantity);

    void delete(Integer id);
}

package com.drinkshop.service.impl;

import com.drinkshop.model.Ingredient;
import com.drinkshop.repository.IngredientRepository;
import com.drinkshop.service.IngredientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class IngredientServiceImpl implements IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientServiceImpl(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> all() {
        return ingredientRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Ingredient> lowStock() {
        return ingredientRepository.findLowStock();
    }

    @Override
    @Transactional(readOnly = true)
    public Ingredient getById(Integer id) {
        return ingredientRepository.findById(id).orElse(null);
    }

    @Override
    public Ingredient save(Ingredient ingredient) {
        if (ingredient.getId() == null && ingredient.getUpdatedAt() == null) {
            ingredient.setUpdatedAt(LocalDateTime.now());
        }
        return ingredientRepository.save(ingredient);
    }

    @Override
    public Ingredient restock(Integer id, BigDecimal quantity) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nguyên liệu không tồn tại."));
        if (quantity != null && quantity.signum() > 0) {
            ingredient.setStockQty(ingredient.getStockQty().add(quantity));
            ingredient.setUpdatedAt(LocalDateTime.now());
        }
        return ingredientRepository.save(ingredient);
    }

    @Override
    public void delete(Integer id) {
        ingredientRepository.deleteById(id);
    }
}

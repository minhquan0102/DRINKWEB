package com.drinkshop.repository;

import com.drinkshop.model.Topping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ToppingRepository extends JpaRepository<Topping, Integer> {

    List<Topping> findByIsAvailableTrueOrderByNameAsc();

    List<Topping> findAllByOrderByNameAsc();
}

package com.drinkshop.service;

import com.drinkshop.model.Category;
import com.drinkshop.model.Product;
import com.drinkshop.model.ProductSize;
import com.drinkshop.model.Review;
import com.drinkshop.model.Topping;

import java.util.List;

/** Nghiệp vụ menu: danh mục, đồ uống, size, topping, đánh giá. */
public interface ProductService {

    // ---------- Dành cho khách ----------
    List<Category> activeCategories();

    List<Product> availableProducts();

    List<Product> productsByCategory(Integer categoryId);

    List<Product> searchProducts(String keyword);

    Product getProduct(Integer id);

    List<ProductSize> allSizes();

    List<Topping> availableToppings();

    List<Review> reviewsOf(Integer productId);

    double averageRating(Integer productId);

    // ---------- Dành cho quản trị ----------
    List<Product> allProducts();

    List<Category> allCategories();

    Product saveProduct(Product product);

    Category saveCategory(Category category);

    void deleteProduct(Integer id);

    void deleteCategory(Integer id);
}

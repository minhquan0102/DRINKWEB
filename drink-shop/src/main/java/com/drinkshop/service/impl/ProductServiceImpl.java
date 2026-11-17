package com.drinkshop.service.impl;

import com.drinkshop.model.Category;
import com.drinkshop.model.Product;
import com.drinkshop.model.ProductSize;
import com.drinkshop.model.Review;
import com.drinkshop.model.Topping;
import com.drinkshop.repository.CategoryRepository;
import com.drinkshop.repository.ProductRepository;
import com.drinkshop.repository.ProductSizeRepository;
import com.drinkshop.repository.ReviewRepository;
import com.drinkshop.repository.ToppingRepository;
import com.drinkshop.service.ProductService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ToppingRepository toppingRepository;
    private final ReviewRepository reviewRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              CategoryRepository categoryRepository,
                              ProductSizeRepository productSizeRepository,
                              ToppingRepository toppingRepository,
                              ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productSizeRepository = productSizeRepository;
        this.toppingRepository = toppingRepository;
        this.reviewRepository = reviewRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> activeCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> availableProducts() {
        return productRepository.findByIsAvailableTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> productsByCategory(Integer categoryId) {
        if (categoryId == null) {
            return availableProducts();
        }
        return productRepository.findByIsAvailableTrueAndCategory_IdOrderByNameAsc(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return availableProducts();
        }
        return productRepository.searchByName(keyword.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProduct(Integer id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductSize> allSizes() {
        return productSizeRepository.findAllByOrderByPriceDeltaAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Topping> availableToppings() {
        return toppingRepository.findByIsAvailableTrueOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> reviewsOf(Integer productId) {
        return reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId);
    }

    @Override
    @Transactional(readOnly = true)
    public double averageRating(Integer productId) {
        Double avg = reviewRepository.averageRating(productId);
        return avg == null ? 0d : Math.round(avg * 10d) / 10d;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> allProducts() {
        return productRepository.findAllByOrderByNameAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> allCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc();
    }

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Override
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    @Override
    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}

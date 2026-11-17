package com.drinkshop.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Bảng products - đồ uống trong menu. */
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /** Giá size M. */
    @Column(name = "base_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Product() {
    }

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (basePrice == null) {
            basePrice = BigDecimal.ZERO;
        }
    }

    /** Ảnh hiển thị: dùng ảnh khai báo trong CSDL, nếu chưa có thì lấy ảnh minh hoạ theo danh mục. */
    public String getDisplayImage() {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }
        String name = category != null && category.getName() != null
                ? category.getName().toLowerCase()
                : "";
        if (name.contains("trà sữa")) {
            return "/images/drinks/tra-sua.svg";
        }
        if (name.contains("cà phê")) {
            return "/images/drinks/ca-phe.svg";
        }
        if (name.contains("trà")) {
            return "/images/drinks/tra-trai-cay.svg";
        }
        if (name.contains("sinh tố") || name.contains("nước ép")) {
            return "/images/drinks/sinh-to.svg";
        }
        if (name.contains("đá xay")) {
            return "/images/drinks/da-xay.svg";
        }
        return "/images/drinks/mac-dinh.svg";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

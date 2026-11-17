package com.drinkshop.service;

import com.drinkshop.model.dto.Cart;

import java.util.List;

/** Nghiệp vụ giỏ hàng (giỏ lưu trong session). */
public interface CartService {

    /**
     * Thêm 1 ly vào giỏ. Nếu trùng product + size + topping + đường/đá thì cộng dồn số lượng.
     *
     * @throws IllegalArgumentException khi sản phẩm không tồn tại hoặc đang hết hàng
     */
    void addToCart(Cart cart, Integer productId, Integer sizeId, int quantity,
                   List<Integer> toppingIds, String sugarLevel, String iceLevel, String note);

    void updateQuantity(Cart cart, String lineId, int quantity);

    void removeItem(Cart cart, String lineId);

    void clear(Cart cart);
}

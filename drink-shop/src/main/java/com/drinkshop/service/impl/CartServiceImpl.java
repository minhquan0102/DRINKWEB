package com.drinkshop.service.impl;

import com.drinkshop.model.Product;
import com.drinkshop.model.ProductSize;
import com.drinkshop.model.Topping;
import com.drinkshop.model.dto.Cart;
import com.drinkshop.model.dto.CartItem;
import com.drinkshop.repository.ProductRepository;
import com.drinkshop.repository.ProductSizeRepository;
import com.drinkshop.repository.ToppingRepository;
import com.drinkshop.service.CartService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    private static final List<String> LEVELS = List.of("0%", "30%", "50%", "70%", "100%");

    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ToppingRepository toppingRepository;

    public CartServiceImpl(ProductRepository productRepository,
                           ProductSizeRepository productSizeRepository,
                           ToppingRepository toppingRepository) {
        this.productRepository = productRepository;
        this.productSizeRepository = productSizeRepository;
        this.toppingRepository = toppingRepository;
    }

    @Override
    public void addToCart(Cart cart, Integer productId, Integer sizeId, int quantity,
                          List<Integer> toppingIds, String sugarLevel, String iceLevel, String note) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đồ uống."));
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Món \"" + product.getName() + "\" đang tạm hết hàng.");
        }

        ProductSize size = productSizeRepository.findById(sizeId)
                .orElseThrow(() -> new IllegalArgumentException("Vui lòng chọn size."));

        int qty = Math.max(1, Math.min(quantity, 50));
        String sugar = normalizeLevel(sugarLevel);
        String ice = normalizeLevel(iceLevel);

        List<Integer> cleanToppingIds = toppingIds == null ? List.of() : toppingIds.stream().distinct().toList();
        List<Topping> toppings = cleanToppingIds.isEmpty()
                ? List.of()
                : toppingRepository.findAllById(cleanToppingIds).stream().filter(Topping::isAvailable).toList();

        BigDecimal unitPrice = product.getBasePrice().add(size.getPriceDelta());
        BigDecimal toppingPrice = toppings.stream()
                .map(Topping::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (CartItem existing : cart.getItems()) {
            if (sameLine(existing, product.getId(), size.getId(), sugar, ice, cleanToppingIds)) {
                existing.setQuantity(Math.min(existing.getQuantity() + qty, 50));
                return;
            }
        }

        CartItem item = new CartItem();
        item.setProductId(product.getId());
        item.setProductName(product.getName());
        item.setImageUrl(product.getDisplayImage());
        item.setSizeId(size.getId());
        item.setSizeName(size.getName());
        item.setQuantity(qty);
        item.setToppingIds(new ArrayList<>(cleanToppingIds));
        item.setToppingNames(toppings.stream().map(Topping::getName).toList());
        item.setToppingPrice(toppingPrice);
        item.setUnitPrice(unitPrice.add(toppingPrice));
        item.setSugarLevel(sugar);
        item.setIceLevel(ice);
        item.setNote(note == null || note.isBlank() ? null : truncate(note.trim(), 255));

        cart.getItems().add(item);
    }

    @Override
    public void updateQuantity(Cart cart, String lineId, int quantity) {
        for (CartItem item : cart.getItems()) {
            if (item.getLineId().equals(lineId)) {
                if (quantity <= 0) {
                    cart.getItems().remove(item);
                } else {
                    item.setQuantity(Math.min(quantity, 50));
                }
                return;
            }
        }
    }

    @Override
    public void removeItem(Cart cart, String lineId) {
        cart.getItems().removeIf(item -> item.getLineId().equals(lineId));
    }

    @Override
    public void clear(Cart cart) {
        cart.clear();
    }

    private boolean sameLine(CartItem item, Integer productId, Integer sizeId, String sugar, String ice,
                             List<Integer> toppingIds) {
        return item.getProductId().equals(productId)
                && item.getSizeId().equals(sizeId)
                && item.getSugarLevel().equals(sugar)
                && item.getIceLevel().equals(ice)
                && item.getToppingIds().stream().sorted().toList()
                        .equals(toppingIds.stream().sorted().toList());
    }

    private String normalizeLevel(String level) {
        return level != null && LEVELS.contains(level) ? level : "100%";
    }

    private String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}

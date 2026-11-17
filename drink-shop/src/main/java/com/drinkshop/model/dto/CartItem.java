package com.drinkshop.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Một dòng trong giỏ hàng: 1 ly đồ uống với size / topping / mức đường đá riêng.
 */
public class CartItem {

    private String lineId = UUID.randomUUID().toString();

    private Integer productId;
    private String productName;
    private String imageUrl;

    private Integer sizeId;
    private String sizeName;

    private Integer quantity = 1;

    /** Giá 1 ly đã gồm size + topping. */
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** Tiền topping của 1 ly (để hiển thị). */
    private BigDecimal toppingPrice = BigDecimal.ZERO;

    private List<Integer> toppingIds = new ArrayList<>();
    private List<String> toppingNames = new ArrayList<>();

    private String sugarLevel = "100%";
    private String iceLevel = "100%";
    private String note;

    public BigDecimal getLineTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public String getToppingText() {
        return toppingNames.isEmpty() ? "Không topping" : String.join(", ", toppingNames);
    }

    public String getLineId() {
        return lineId;
    }

    public void setLineId(String lineId) {
        this.lineId = lineId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getSizeId() {
        return sizeId;
    }

    public void setSizeId(Integer sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeName() {
        return sizeName;
    }

    public void setSizeName(String sizeName) {
        this.sizeName = sizeName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getToppingPrice() {
        return toppingPrice;
    }

    public void setToppingPrice(BigDecimal toppingPrice) {
        this.toppingPrice = toppingPrice;
    }

    public List<Integer> getToppingIds() {
        return toppingIds;
    }

    public void setToppingIds(List<Integer> toppingIds) {
        this.toppingIds = toppingIds;
    }

    public List<String> getToppingNames() {
        return toppingNames;
    }

    public void setToppingNames(List<String> toppingNames) {
        this.toppingNames = toppingNames;
    }

    public String getSugarLevel() {
        return sugarLevel;
    }

    public void setSugarLevel(String sugarLevel) {
        this.sugarLevel = sugarLevel;
    }

    public String getIceLevel() {
        return iceLevel;
    }

    public void setIceLevel(String iceLevel) {
        this.iceLevel = iceLevel;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

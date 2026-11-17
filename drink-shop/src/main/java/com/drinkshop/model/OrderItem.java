package com.drinkshop.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Bảng order_items - chi tiết từng ly trong đơn. */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private ProductSize size;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 1;

    /** Giá tại thời điểm bán = base_price + price_delta. */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 0)
    private BigDecimal unitPrice = BigDecimal.ZERO;

    /** Mức đường: 0%, 30%, 50%, 70%, 100%. */
    @Column(name = "sugar_level", nullable = false, length = 5)
    private String sugarLevel = "100%";

    /** Mức đá: 0%, 30%, 50%, 70%, 100%. */
    @Column(name = "ice_level", nullable = false, length = 5)
    private String iceLevel = "100%";

    @Column(name = "line_total", nullable = false, precision = 12, scale = 0)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(name = "note", length = 255)
    private String note;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItemTopping> toppings = new ArrayList<>();

    public OrderItem() {
    }

    public void addTopping(OrderItemTopping topping) {
        topping.setOrderItem(this);
        this.toppings.add(topping);
    }

    /** Tên topping dạng "Trân châu đen, Thạch dừa". */
    public String getToppingNames() {
        if (toppings == null || toppings.isEmpty()) {
            return "";
        }
        return String.join(", ", toppings.stream().map(t -> t.getTopping().getName()).toList());
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public ProductSize getSize() {
        return size;
    }

    public void setSize(ProductSize size) {
        this.size = size;
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

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<OrderItemTopping> getToppings() {
        return toppings;
    }

    public void setToppings(List<OrderItemTopping> toppings) {
        this.toppings = toppings;
    }
}

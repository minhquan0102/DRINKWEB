package com.drinkshop.model.dto;

import com.drinkshop.model.Fulfillment;
import com.drinkshop.model.OrderType;
import com.drinkshop.model.PaymentMethod;

/** Form đặt hàng ở bước thanh toán. */
public class CheckoutForm {

    private OrderType orderType = OrderType.ONLINE;

    private Fulfillment fulfillment = Fulfillment.DELIVERY;

    private PaymentMethod paymentMethod = PaymentMethod.CASH;

    private Integer addressId;

    private String tableNo;

    private String voucherCode;

    private String note;

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public Fulfillment getFulfillment() {
        return fulfillment;
    }

    public void setFulfillment(Fulfillment fulfillment) {
        this.fulfillment = fulfillment;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Integer getAddressId() {
        return addressId;
    }

    public void setAddressId(Integer addressId) {
        this.addressId = addressId;
    }

    public String getTableNo() {
        return tableNo;
    }

    public void setTableNo(String tableNo) {
        this.tableNo = tableNo;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public void setVoucherCode(String voucherCode) {
        this.voucherCode = voucherCode;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

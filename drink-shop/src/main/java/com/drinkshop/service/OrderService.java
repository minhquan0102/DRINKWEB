package com.drinkshop.service;

import com.drinkshop.model.Order;
import com.drinkshop.model.OrderStatus;
import com.drinkshop.model.Payment;
import com.drinkshop.model.dto.Cart;
import com.drinkshop.model.dto.CheckoutForm;

import java.util.List;

/** Nghiệp vụ đơn hàng: đặt đơn, theo dõi, cập nhật trạng thái. */
public interface OrderService {

    /**
     * Tạo đơn từ giỏ hàng.
     *
     * @param userId id người dùng đang đăng nhập (có thể null nếu khách vãng lai)
     * @throws IllegalStateException khi giỏ trống hoặc voucher không hợp lệ
     */
    Order placeOrder(Integer userId, Cart cart, CheckoutForm form);

    Order getById(Integer id);

    Order getByCode(String code);

    List<Order> ordersOfCustomer(Integer userId);

    List<Order> allOrders();

    /** Đơn đang xử lý (PENDING/CONFIRMED/PREPARING/READY/DELIVERING). */
    List<Order> activeOrders();

    void updateStatus(Integer orderId, OrderStatus newStatus, Integer staffUserId);

    /** Các trạng thái được phép chuyển tới từ trạng thái hiện tại. */
    List<OrderStatus> nextStatuses(OrderStatus current);

    Payment getPayment(Integer orderId);

    /**
     * Đánh dấu đơn đã thanh toán thành công (dùng cho cổng thanh toán online).
     *
     * @return bản ghi thanh toán sau khi cập nhật, null nếu đơn không có payment
     */
    Payment markPaid(String orderCode, String transactionCode, String methodNote);

    /** Đánh dấu giao dịch thất bại / bị huỷ (đơn vẫn giữ để khách thanh toán lại). */
    Payment markFailed(String orderCode, String responseCode);

    long countByStatus(OrderStatus status);
}

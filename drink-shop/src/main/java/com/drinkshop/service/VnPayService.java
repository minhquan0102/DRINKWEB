package com.drinkshop.service;

import com.drinkshop.model.Order;

import java.util.Map;

/** Tạo URL thanh toán VNPay và kiểm tra chữ ký của dữ liệu trả về. */
public interface VnPayService {

    /** Sinh URL cổng thanh toán cho đơn hàng. */
    String createPaymentUrl(Order order, String ipAddress);

    /** Bộ tham số trả về hợp lệ (đã ký) — dùng cho trang mô phỏng. */
    Map<String, String> buildSimulatedReturn(Order order, boolean success);

    /** Kiểm tra vnp_SecureHash có khớp với dữ liệu nhận được hay không. */
    boolean verifySignature(Map<String, String> params);

    /** true nếu đang chạy chế độ mô phỏng (không gọi VNPay thật). */
    boolean isSimulate();

    /** Query string (đã mã hoá) từ bộ tham số — dùng để chuyển về return URL tương đối. */
    String buildReturnQuery(Map<String, String> params);
}

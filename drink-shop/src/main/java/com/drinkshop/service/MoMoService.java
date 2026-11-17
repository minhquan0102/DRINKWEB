package com.drinkshop.service;

import com.drinkshop.model.Order;

import java.util.Map;

/** Tạo giao dịch MoMo và kiểm tra chữ ký dữ liệu trả về (HMAC-SHA256). */
public interface MoMoService {

    /** Gọi API tạo giao dịch, trả về payUrl để chuyển trình duyệt sang MoMo. */
    String createPaymentUrl(Order order);

    /** Kiểm tra chữ ký của dữ liệu redirect / IPN. */
    boolean verifySignature(Map<String, String> data);

    /** Bộ tham số trả về hợp lệ (đã ký) cho trang mô phỏng. */
    Map<String, String> buildSimulatedReturn(Order order, boolean success);

    boolean isSimulate();
}

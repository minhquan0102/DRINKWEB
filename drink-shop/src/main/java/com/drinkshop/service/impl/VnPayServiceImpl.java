package com.drinkshop.service.impl;

import com.drinkshop.config.VnPayProperties;
import com.drinkshop.model.Order;
import com.drinkshop.service.VnPayService;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Cài đặt theo đặc tả VNPay 2.1.0:
 * tham số sắp xếp A→Z, dữ liệu ký là chuỗi "key=value" đã URL-encode, nối bằng '&',
 * ký HMAC-SHA512 bằng hash-secret, kết quả là chuỗi hex.
 */
@Service
public class VnPayServiceImpl implements VnPayService {

    private static final DateTimeFormatter VNPAY_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final VnPayProperties properties;

    public VnPayServiceImpl(VnPayProperties properties) {
        this.properties = properties;
    }

    @Override
    public String createPaymentUrl(Order order, String ipAddress) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", properties.getVersion());
        params.put("vnp_Command", properties.getCommand());
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_Locale", properties.getLocale());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", order.getOrderCode());
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        params.put("vnp_OrderType", properties.getOrderType());
        params.put("vnp_Amount", String.valueOf(toVnPayAmount(order.getTotalAmount())));
        params.put("vnp_ReturnUrl", properties.getReturnUrl());
        params.put("vnp_IpAddr", ipAddress == null || ipAddress.isBlank() ? "127.0.0.1" : ipAddress);
        params.put("vnp_CreateDate", LocalDateTime.now().format(VNPAY_FORMAT));

        String secureHash = hmacSha512(hashAllFields(params));
        return properties.getPayUrl() + "?" + queryString(params) + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public Map<String, String> buildSimulatedReturn(Order order, boolean success) {
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_TmnCode", properties.getTmnCode());
        params.put("vnp_TxnRef", order.getOrderCode());
        params.put("vnp_Amount", String.valueOf(toVnPayAmount(order.getTotalAmount())));
        params.put("vnp_OrderInfo", "Thanh toan don hang " + order.getOrderCode());
        params.put("vnp_ResponseCode", success ? "00" : "24");
        params.put("vnp_TransactionStatus", success ? "00" : "02");
        params.put("vnp_TransactionNo", "SIM" + System.currentTimeMillis());
        params.put("vnp_BankCode", "NCB");
        params.put("vnp_PayDate", LocalDateTime.now().format(VNPAY_FORMAT));
        params.put("vnp_SecureHash", hmacSha512(hashAllFields(params)));
        return params;
    }

    @Override
    public boolean verifySignature(Map<String, String> params) {
        String received = params.get("vnp_SecureHash");
        if (received == null || received.isBlank()) {
            return false;
        }
        // TreeMap để dữ liệu ký luôn được sắp xếp A→Z giống lúc tạo URL
        Map<String, String> copy = new TreeMap<>(params);
        copy.remove("vnp_SecureHash");
        copy.remove("vnp_SecureHashType");
        String data = hashAllFields(copy);

        // Một số cấu hình merchant để "Kiểu mã hóa = MD5" cho IPN:
        // chữ ký MD5 dài 32 ký tự, SHA512 dài 128 ký tự => chấp nhận cả hai.
        if (received.length() == 32) {
            return md5Hex(data).equalsIgnoreCase(received);
        }
        return hmacSha512(data).equalsIgnoreCase(received);
    }

    @Override
    public boolean isSimulate() {
        return properties.isSimulate();
    }

    @Override
    public String buildReturnQuery(Map<String, String> params) {
        return queryString(params);
    }

    // ---------------- tiện ích ----------------

    private long toVnPayAmount(BigDecimal amount) {
        return amount == null ? 0L : amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    /** "key1=value1&key2=value2" theo thứ tự A→Z, value đã URL-encode (dùng để ký). */
    private String hashAllFields(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) {
                continue;
            }
            if (!first) {
                sb.append('&');
            }
            sb.append(entry.getKey()).append('=').append(encode(value));
            first = false;
        }
        return sb.toString();
    }

    private String queryString(Map<String, String> params) {
        return hashAllFields(params);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String hmacSha512(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(properties.getHashSecret().getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không ký được dữ liệu VNPay", ex);
        }
    }

    /** MD5 checksum (chữ in hoa) — dùng cho merchant cấu hình IPN kiểu mã hoá MD5. */
    private String md5Hex(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] raw = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02X", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không tạo được MD5 checksum", ex);
        }
    }
}

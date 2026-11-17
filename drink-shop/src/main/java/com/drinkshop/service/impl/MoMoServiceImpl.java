package com.drinkshop.service.impl;

import com.drinkshop.config.MoMoProperties;
import com.drinkshop.model.Order;
import com.drinkshop.service.MoMoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tích hợp MoMo Payment API v2.
 *
 * <p>Chữ ký là HMAC-SHA256 trên chuỗi tham số sắp xếp A→Z:
 * {@code accessKey=..&amount=..&extraData=..&ipnUrl=..&orderId=..&orderInfo=..&partnerCode=..&redirectUrl=..&requestId=..&requestType=..}</p>
 */
@Service
public class MoMoServiceImpl implements MoMoService {

    private static final Logger log = LoggerFactory.getLogger(MoMoServiceImpl.class);

    private final MoMoProperties properties;
    private final RestClient restClient;

    public MoMoServiceImpl(MoMoProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder().build();
    }

    @Override
    public String createPaymentUrl(Order order) {
        String orderId = order.getOrderCode();
        String requestId = UUID.randomUUID().toString();
        String amount = String.valueOf(order.getTotalAmount().longValue());
        String orderInfo = "Thanh toan don hang " + orderId;
        String extraData = "";

        String raw = "accessKey=" + properties.getAccessKey()
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + properties.getIpnUrl()
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + properties.getPartnerCode()
                + "&redirectUrl=" + properties.getRedirectUrl()
                + "&requestId=" + requestId
                + "&requestType=" + properties.getRequestType();

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("partnerCode", properties.getPartnerCode());
        payload.put("partnerName", "Drink Shop");
        payload.put("storeId", "DrinkShopStore");
        payload.put("requestId", requestId);
        payload.put("amount", order.getTotalAmount().longValue());
        payload.put("orderId", orderId);
        payload.put("orderInfo", orderInfo);
        payload.put("redirectUrl", properties.getRedirectUrl());
        payload.put("ipnUrl", properties.getIpnUrl());
        payload.put("lang", properties.getLang());
        payload.put("extraData", extraData);
        payload.put("requestType", properties.getRequestType());
        payload.put("signature", hmacSha256(raw));

        try {
            Map<?, ?> result = restClient.post()
                    .uri(properties.getEndpoint())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(payload)
                    .retrieve()
                    .body(Map.class);

            if (result == null) {
                throw new IllegalStateException("MoMo không trả về dữ liệu.");
            }
            Object resultCode = result.get("resultCode");
            if (resultCode == null || !"0".equals(String.valueOf(resultCode))) {
                throw new IllegalStateException("MoMo từ chối giao dịch: " + result.get("message"));
            }
            Object payUrl = result.get("payUrl");
            if (payUrl == null || payUrl.toString().isBlank()) {
                throw new IllegalStateException("MoMo không trả về payUrl.");
            }
            return payUrl.toString();
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (RestClientResponseException ex) {
            log.error("MoMo trả lỗi HTTP {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new IllegalStateException("Cổng MoMo trả lỗi, vui lòng thử lại.");
        } catch (RuntimeException ex) {
            log.error("Không tạo được giao dịch MoMo cho đơn {}", orderId, ex);
            throw new IllegalStateException("Không kết nối được cổng MoMo. Vui lòng thử lại.");
        }
    }

    @Override
    public boolean verifySignature(Map<String, String> data) {
        String received = value(data, "signature");
        if (received == null || received.isBlank()) {
            return false;
        }
        String raw = rawForVerify(data);
        return hmacSha256(raw).equalsIgnoreCase(received);
    }

    @Override
    public Map<String, String> buildSimulatedReturn(Order order, boolean success) {
        String resultCode = success ? "0" : "1006";
        Map<String, String> data = new LinkedHashMap<>();
        data.put("partnerCode", properties.getPartnerCode());
        data.put("orderId", order.getOrderCode());
        data.put("requestId", UUID.randomUUID().toString());
        data.put("amount", String.valueOf(order.getTotalAmount().longValue()));
        data.put("orderInfo", "Thanh toan don hang " + order.getOrderCode());
        data.put("orderType", "momo_wallet");
        data.put("transId", String.valueOf(System.currentTimeMillis()));
        data.put("resultCode", resultCode);
        data.put("message", success ? "Thành công." : "Giao dịch bị huỷ.");
        data.put("payType", "qr");
        data.put("responseTime", String.valueOf(System.currentTimeMillis()));
        data.put("extraData", "");
        data.put("signature", hmacSha256(rawForVerify(data)));
        return data;
    }

    @Override
    public boolean isSimulate() {
        return properties.isSimulate();
    }

    // ---------------- tiện ích ----------------

    /** Chuỗi dữ liệu ký của dữ liệu trả về (redirect / IPN), sắp xếp A→Z. */
    private String rawForVerify(Map<String, String> data) {
        return "accessKey=" + properties.getAccessKey()
                + "&amount=" + value(data, "amount")
                + "&extraData=" + value(data, "extraData")
                + "&message=" + value(data, "message")
                + "&orderId=" + value(data, "orderId")
                + "&orderInfo=" + value(data, "orderInfo")
                + "&orderType=" + value(data, "orderType")
                + "&partnerCode=" + value(data, "partnerCode")
                + "&payType=" + value(data, "payType")
                + "&requestId=" + value(data, "requestId")
                + "&responseTime=" + value(data, "responseTime")
                + "&resultCode=" + value(data, "resultCode")
                + "&transId=" + value(data, "transId");
    }

    private String value(Map<String, String> data, String key) {
        String value = data.get(key);
        return value == null ? "" : value;
    }

    private String hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(properties.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("Không ký được dữ liệu MoMo", ex);
        }
    }
}

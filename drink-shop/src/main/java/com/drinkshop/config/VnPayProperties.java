package com.drinkshop.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Cấu hình cổng thanh toán VNPay.
 *
 * <p>Mặc định chạy ở chế độ MÔ PHỎNG (simulate = true) để demo được ngay khi chưa có
 * merchant sandbox. Khi có tài khoản sandbox thật, điền tmn-code + hash-secret và
 * đặt app.vnpay.simulate=false trong application.properties.</p>
 */
@Component
@ConfigurationProperties(prefix = "app.vnpay")
public class VnPayProperties {

    /** Mã website (Terminal ID) do VNPay cấp. */
    private String tmnCode = "DEMO0001";

    /** Chuỗi bí mật dùng để ký HMAC-SHA512. */
    private String hashSecret = "DEMOSECRETHASH512KEY0123456789ABCDEF";

    /** Địa chỉ cổng thanh toán (sandbox). */
    private String payUrl = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    /** URL nhận kết quả sau khi thanh toán. */
    private String returnUrl = "http://localhost:8080/payment/vnpay/return";

    /** URL IPN — VNPay gọi server-to-server để cập nhật trạng thái giao dịch. */
    private String ipnUrl = "http://localhost:8080/payment/vnpay/ipn";

    /** true = dùng trang mô phỏng nội bộ thay vì gọi VNPay thật. */
    private boolean simulate = false;

    private String version = "2.1.0";
    private String command = "pay";
    private String orderType = "other";
    private String locale = "vn";

    public String getTmnCode() {
        return tmnCode;
    }

    public void setTmnCode(String tmnCode) {
        this.tmnCode = tmnCode;
    }

    public String getHashSecret() {
        return hashSecret;
    }

    public void setHashSecret(String hashSecret) {
        this.hashSecret = hashSecret;
    }

    public String getPayUrl() {
        return payUrl;
    }

    public void setPayUrl(String payUrl) {
        this.payUrl = payUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getIpnUrl() {
        return ipnUrl;
    }

    public void setIpnUrl(String ipnUrl) {
        this.ipnUrl = ipnUrl;
    }

    public boolean isSimulate() {
        return simulate;
    }

    public void setSimulate(boolean simulate) {
        this.simulate = simulate;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}

# Kích hoạt VNPay Sandbox cho Drink Shop

## 1. Thông tin merchant (đã cấu hình trong `application.properties`)

| Tham số | Giá trị |
|---|---|
| `vnp_TmnCode` | `TZE62LK1` |
| `vnp_HashSecret` | (chuỗi bí mật VNPay cấp — không commit public) |
| `vnp_Url` | `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html` |
| Return URL | `http://localhost:8080/payment/vnpay/return` (override `VNPAY_RETURN_URL`) |
| IPN URL | `http://localhost:8080/payment/vnpay/ipn` (override `VNPAY_IPN_URL`) |

## 2. Lỗi `code=71` — "The terminal (website) not approved"

Đây là lỗi **phía merchant**, không phải code:

- Chữ ký HMAC-SHA512 của app đã đúng (sửa 1 ký tự hash → VNPay vẫn trả cùng lỗi 71,
  nghĩa là VNPay chặn trước khi kiểm tra chữ ký).
- Đổi `vnp_ReturnUrl` sang domain công khai (example.com) → vẫn 71.
- IPN của app đã được test 11/11 với chính `hash-secret` này.

⇒ Terminal `TZE62LK1` chưa được duyệt website.

## 3. Cách bật terminal (làm trong Merchant Admin)

1. Đăng nhập <https://sandbox.vnpayment.vn/merchantv2/Users/Login.htm>.
2. Mở thẳng trang cấu hình terminal:
   **<https://sandbox.vnpayment.vn/merchantv2/Account/TerminalEdit.htm>**
   (hoặc góc trên bên phải → **Thông tin tài khoản** → danh sách website → biểu tượng **sửa**).
3. Khai báo:
   - **Website**: `http://localhost:8080` (khi chạy máy) hoặc domain/tunnel thật.
     Domain này **phải trùng** domain trong `vnp_ReturnUrl`.
   - **IPN URL**: `http://localhost:8080/payment/vnpay/ipn`
     (khi test server-to-server thì dùng tunnel công khai, xem mục 5).
4. Lưu lại. Nếu trạng thái vẫn *Chờ duyệt*, gửi `hotrovnpay@vnpay.vn`:
   TmnCode `TZE62LK1` + email đăng ký + **mã tra cứu** trên trang lỗi.

Sau khi được duyệt, **không cần sửa code** — chỉ chạy lại và bấm "Thanh toán VNPay".

## 4. Thẻ test

| Ngân hàng | Số thẻ | Tên chủ thẻ | Phát hành | OTP |
|---|---|---|---|---|
| NCB | `9704 1985 2619 1432 198` | `NGUYEN VAN A` | `07/15` | `123456` |

## 5. Test IPN ở máy cá nhân (cần URL công khai)

```bat
cloudflared tunnel --url http://localhost:8080
set VNPAY_RETURN_URL=https://<ten-tunnel>.trycloudflare.com/payment/vnpay/return
set VNPAY_IPN_URL=https://<ten-tunnel>.trycloudflare.com/payment/vnpay/ipn
```

Dùng đúng domain tunnel khi khai báo website + IPN ở mục 3.

## 6. Trong lúc chờ duyệt

Đặt `app.vnpay.simulate=true` để demo luồng thanh toán (vẫn ký/kiểm tra HMAC-SHA512 thật,
chỉ thay giao diện cổng VNPay bằng trang mô phỏng nội bộ).

## 7. Kiểm tra nhanh bằng API IPN (không cần trình duyệt)

```bat
:: sau khi có đơn PENDING, gọi IPN với chữ ký đúng sẽ trả {"RspCode":"00"}
curl "http://localhost:8080/payment/vnpay/ipn?vnp_TxnRef=<ma-don>&vnp_Amount=<tien*100>&..."
```

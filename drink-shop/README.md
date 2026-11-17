# Drink Shop — Ứng dụng bán nước giải khát

Project Spring Boot + Thymeleaf + MySQL dựng trên CSDL `drink_shop` (script `app_ban_nuoc.sql`).

| Thành phần | Phiên bản |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.2 |
| Spring Security | 7.1.2 |
| Hibernate | 7.4.13 |
| Thymeleaf | 3.1 |
| MySQL | 8.4 |

---

## 1. Cấu trúc project

```
drink-shop/
├── pom.xml
├── run.bat / build.bat            # chạy bằng Maven đi kèm (không cần cài Maven)
└── src/main/
    ├── java/com/drinkshop/
    │   ├── DrinkShopApplication.java
    │   ├── config/                 # SecurityConfig, DataInitializer, CurrentUser,
    │   │                           # ViewFormatter, GlobalModelAttributes
    │   ├── controller/             # Home, Auth, Cart, Checkout, Order
    │   │   └── admin/              # Dashboard, Catalog, Voucher, Ingredient, Order
    │   ├── model/                  # 16 entity + enum
    │   │   └── dto/                # Cart, CartItem, CheckoutForm, RegisterForm, ...
    │   ├── repository/             # 16 interface Spring Data JPA
    │   └── service/               # interface nghiệp vụ
    │       └── impl/               # lớp cài đặt (@Service)
    └── resources/
        ├── application.properties
        ├── static/css/app.css, static/images/...
        └── templates/              # Thymeleaf: index, menu, product-detail, cart,
                                    # checkout, orders, order-detail, login, register,
                                    # 403, admin/*
```

Đúng mô hình bạn yêu cầu: **controller – model – repository – service – impl**, view dùng **Thymeleaf**.

---

## 2. Cấu hình CSDL

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/drink_shop?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=none
```

Vì CSDL đã tạo sẵn bằng script nên để `ddl-auto=none` (Hibernate không sửa cấu trúc bảng).

Nếu MySQL của bạn dùng mật khẩu khác, chỉ cần sửa `spring.datasource.password`.

---

## 3. Chạy ứng dụng

### Cách 1 — dùng script kèm sẵn (khuyến nghị, không cần cài Maven)

```bat
cd D:\AMLO\drink-shop
run.bat
```

Sau đó mở <http://localhost:8080>.

`run.bat` dùng Maven + kho thư viện nằm trong `D:\AMLO\tools`, nên chạy được cả khi mạng chậm.

### Cách 2 — bằng Maven đã cài

```bat
mvn spring-boot:run
```

### Cách 3 — từ IntelliJ IDEA / Eclipse

1. `File → Open` chọn thư mục `drink-shop` (chọn file `pom.xml` nếu được hỏi).
2. Chờ IDE tải dependency.
3. Chạy class `com.drinkshop.DrinkShopApplication`.
4. Truy cập <http://localhost:8080>.

### Đóng gói thành file jar

```bat
build.bat
java -jar target\drink-shop-1.0.0.jar
```

---

## 4. Tài khoản đăng nhập

Đăng nhập bằng **số điện thoại** + mật khẩu. Script SQL chỉ có hash giả, nên khi khởi động
`DataInitializer` tự băm lại mật khẩu cho toàn bộ tài khoản demo.

**Mật khẩu chung: `123456`**

| Số điện thoại | Vai trò | Vào được |
|---|---|---|
| 0901000001 | Chủ quán (OWNER) | Tất cả: báo cáo, menu, kho, voucher, đơn |
| 0901000002 | Quản lý (MANAGER) | Như chủ quán |
| 0901000003 | Thu ngân (CASHIER) | Xử lý đơn hàng |
| 0901000004 | Pha chế (BARISTA) | Xử lý đơn hàng |
| 0912000001 | Khách hàng (CUSTOMER) | Menu, giỏ hàng, đặt đơn, xem đơn |

Khách mới có thể tự đăng ký ở `/register`.

---

## 5. Chức năng đã có

**Khách hàng**
- Xem menu theo danh mục, tìm kiếm, xem chi tiết + đánh giá
- Chọn size, mức đường, mức đá, topping, số lượng, ghi chú
- Giỏ hàng lưu trong session (thêm/sửa/xoá)
- Thanh toán: kênh bán (online / tại quầy), hình thức (giao hàng / mang đi / tại chỗ),
  địa chỉ giao hàng (thêm mới được), mã giảm giá, phương thức thanh toán
- Xem danh sách đơn, chi tiết đơn, tự huỷ đơn khi còn *Chờ xác nhận*
- Đăng ký tài khoản mới

**Nhân viên / chủ quán**
- Danh sách đơn + lọc theo trạng thái, chuyển trạng thái theo đúng luồng:
  `PENDING → CONFIRMED → PREPARING → READY → DELIVERING → COMPLETED` (hoặc `CANCELLED`)
- Khi đơn hoàn thành: thanh toán COD tự chuyển *Đã thanh toán*, khách được cộng điểm
  (1 điểm / 10.000đ)
- Bảng điều khiển: doanh thu hôm nay, số đơn, đơn chờ, khách hàng, món đang bán
- Báo cáo doanh thu theo ngày & kênh bán, món bán chạy, nguyên liệu sắp hết
  (đọc trực tiếp 3 VIEW của CSDL)
- CRUD đồ uống, danh mục, mã giảm giá, nguyên liệu + nhập thêm hàng vào kho

---

### 5.1 Quản lý tài khoản (mới)

- Trang **`/admin/users`** (chủ quán / quản lý): danh sách toàn bộ tài khoản, tìm theo
  tên – SĐT – email, lọc theo vai trò và trạng thái, thống kê nhanh tổng/khách/nhân sự/đang khoá.
- **Tạo tài khoản** (`/admin/users/new`): chọn vai trò (chủ quán, quản lý, thu ngân, pha chế,
  khách hàng), đặt mật khẩu; hệ thống tự tạo bản ghi `customers` hoặc `staff` tương ứng.
- **Sửa tài khoản**: đổi họ tên, SĐT, email, vai trò, trạng thái; ô mật khẩu để trống = giữ nguyên,
  nhập mới = đặt lại mật khẩu.
- **Khoá / mở khoá** một chạm; tài khoản bị khoá không đăng nhập được (Spring Security `disabled`).
- **Hồ sơ cá nhân** (`/profile`, bấm vào tên trên header): cập nhật họ tên, email, ngày sinh
  (khách hàng), đổi mật khẩu (kiểm tra mật khẩu hiện tại), xem điểm tích luỹ và địa chỉ đã lưu.

### 5.2 Thanh toán online VNPay (mới)

Luồng: chọn **VNPay** ở bước thanh toán → tạo đơn + bản ghi `payments` (PENDING) →
chuyển sang cổng thanh toán → VNPay gọi lại `return-url` kèm chữ ký → xác thực
HMAC-SHA512 → cập nhật `payments.status = PAID`, lưu `transaction_code` → về trang đơn hàng.

| Thành phần | Mô tả |
|---|---|
| `VnPayProperties` | đọc `app.vnpay.*` (tmn-code, hash-secret, pay-url, return-url, simulate) |
| `VnPayServiceImpl` | tạo URL thanh toán (sắp xếp tham số A→Z, URL-encode, ký HMAC-SHA512) và kiểm tra chữ ký |
| `PaymentController` | `/payment/vnpay/{orderCode}` (khởi tạo), `/payment/vnpay/return` (nhận kết quả), `/payment/vnpay/simulator` (mô phỏng) |
| `OrderService.markPaid` | ghi nhận thanh toán thành công |

Cấu hình trong `application.properties` (đã điền merchant sandbox được VNPay cấp):

```properties
app.vnpay.tmn-code=TZE62LK1
app.vnpay.hash-secret=<chuỗi bí mật VNPay cấp>
app.vnpay.pay-url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
app.vnpay.return-url=${VNPAY_RETURN_URL:http://localhost:8080/payment/vnpay/return}
app.vnpay.ipn-url=${VNPAY_IPN_URL:http://localhost:8080/payment/vnpay/ipn}
app.vnpay.simulate=false
```

- `simulate=true` (**đang bật**): dùng trang mô phỏng nội bộ (vẫn ký/kiểm tra HMAC-SHA512)
  để demo được ngay khi terminal sandbox chưa được duyệt website.
- `simulate=false`: chuyển thật sang `sandbox.vnpayment.vn` — chỉ dùng sau khi VNPay duyệt
  website/terminal (nếu chưa duyệt sẽ gặp lỗi `code=71 – The terminal (website) not approved`).
- **Không đưa `hash-secret` lên GitHub/public** — chỉ để trong máy hoặc biến môi trường.

**IPN (server → server).** VNPay gọi `GET /payment/vnpay/ipn` để xác nhận giao dịch; endpoint trả JSON
theo đặc tả: `00` thành công, `01` không tìm thấy đơn, `02` đã xác nhận, `04` sai số tiền,
`97` sai chữ ký. Endpoint được `permitAll` (VNPay không có session) và kiểm tra:
chữ ký → đơn tồn tại → số tiền khớp (`vnp_Amount = total × 100`) → idempotent.

- Ở máy cá nhân, `localhost` không cho VNPay gọi vào ⇒ dùng tunnel rồi khai báo URL công khai:
  ```bat
  cloudflared tunnel --url http://localhost:8080
  set VNPAY_RETURN_URL=https://<ten-tunnel>.trycloudflare.com/payment/vnpay/return
  set VNPAY_IPN_URL=https://<ten-tunnel>.trycloudflare.com/payment/vnpay/ipn
  ```
  Sau đó gửi URL IPN cho VNPay và nhập vào Merchant Admin.
- **Thẻ test sandbox:** NCB `9704 1985 2619 1432 198`, tên `NGUYEN VAN A`,
  phát hành `07/15`, OTP `123456`.

**Nếu cổng VNPay báo “Website này chưa được phép”:** chữ ký và tham số đã đúng (đã kiểm tra
bằng URL có chữ ký sai — VNPay trả cùng thông báo), nghĩa là **website của terminal chưa được
duyệt/kích hoạt**. Cách xử lý:
1. Đăng nhập <https://sandbox.vnpayment.vn/merchantv2/> → mục **Website/Thông tin merchant**,
   khai báo đúng domain website và **IPN URL**, lưu lại.
2. Nếu vẫn bị chặn, gửi VNPay (`hotrovnpay@vnpay.vn`) mã terminal **TZE62LK1** + mã tra cứu
   hiển thị trên trang lỗi để được kích hoạt.
3. Trong lúc chờ duyệt, đặt `app.vnpay.simulate=true` để demo luồng thanh toán.

- Đơn chưa thanh toán (PENDING/FAILED) có nút **“Thanh toán VNPay”** ngay trong trang chi tiết đơn.

### 5.3 Biểu đồ thống kê (mới)

Chart.js 4.4.7 nhúng sẵn trong `static/js/vendor/chart.umd.min.js` (chạy offline, không cần CDN).
Dữ liệu do Thymeleaf serialize xuống JS, cấu hình vẽ trong `static/js/admin-charts.js`:

| Biểu đồ | Nội dung |
|---|---|
| Đường (area) | Doanh thu 14 ngày gần nhất, tách Online / Tại quầy |
| Tròn (doughnut) | Số đơn theo trạng thái (bỏ nhóm 0) |
| Tròn (doughnut) | Số giao dịch đã thanh toán theo phương thức (VNPay, MoMo, tiền mặt…) |
| Cột ngang | Top 5 đồ uống bán chạy theo số ly |

Có ở **Tổng quan** (`/admin`) và **Báo cáo** (`/admin/reports`).



### 5.4 Thanh toán MoMo (sandbox — chạy thật)

Luồng: chọn **MoMo** → tạo đơn + payment PENDING → app gọi API MoMo (ký **HMAC-SHA256**)
→ chuyển sang `payUrl` của MoMo → quét QR bằng app MoMo test → MoMo redirect về
`/payment/momo/return` (kiểm tra chữ ký + số tiền) → PAID, lưu `transId`.
MoMo đồng thời gọi IPN `POST /payment/momo/ipn` (app trả **204**).

| Thành phần | Mô tả |
|---|---|
| `MoMoProperties` | đọc `app.momo.*` |
| `MoMoServiceImpl` | ký/verify HMAC-SHA256, gọi API tạo giao dịch bằng `RestClient` |
| `MoMoController` | `/payment/momo/{orderCode}`, `/payment/momo/return`, `/payment/momo/ipn` |

```properties
app.momo.partner-code=MOMO
app.momo.access-key=F8BBA842ECF85
app.momo.secret-key=K951B6PE1waDMi640xX08PD3vg6EkVlz
app.momo.endpoint=https://test-payment.momo.vn/v2/gateway/api/create
app.momo.redirect-url=${MOMO_REDIRECT_URL:http://localhost:8080/payment/momo/return}
app.momo.ipn-url=${MOMO_IPN_URL:http://localhost:8080/payment/momo/ipn}
app.momo.request-type=captureWallet
app.momo.simulate=false
```

- Đây là **credential sandbox công khai của MoMo**, dùng được ngay (không cần đăng ký).
  Merchant thật: thay `partner-code` / `access-key` / `secret-key`.
- `simulate=false`: gọi MoMo thật. `simulate=true`: trang mô phỏng nội bộ (vẫn ký HMAC-SHA256).
- **Khác VNPay: số tiền MoMo KHÔNG nhân 100** (`amount` = số VND).
- Thanh toán bằng app MoMo quét QR; MoMo sandbox không trừ tiền thật.
- IPN cần URL công khai khi deploy; chạy localhost thì luồng return đã đủ cập nhật đơn.



## 6. Giao diện

- **Design system** trong `static/css/app.css` (~2.900 dòng): token màu/vị trí, typography,
  button, form, card, table, badge, timeline, sidebar, toast… Giao diện tiếng Việt, tông
  kem – terracotta, bo góc mềm, có phân cấp thị giác rõ ràng.
- **Illustration riêng** cho từng nhóm đồ uống: `static/images/drinks/tra-sua.svg`,
  `ca-phe.svg`, `tra-trai-cay.svg`, `sinh-to.svg`, `da-xay.svg`, `mac-dinh.svg`
  và ảnh hero `static/images/hero-drink.svg`. Ảnh tự gán theo danh mục
  (`Product.getDisplayImage()`), không cần upload ảnh.
- **Header** sticky + menu drawer trên mobile; **sidebar** cho khu quản trị.
- **JS thuần** (`static/js/app.js`): mở menu mobile, stepper số lượng, tự gửi form khi đổi
  số lượng, hộp xác nhận khi xoá, ẩn/hiện khối địa chỉ theo hình thức nhận hàng.
- **Responsive** 3 mốc: ≥1080px (2 cột), 860px (menu mobile, ẩn sidebar thành thanh ngang),
  560px (1 cột, chip cuộn ngang, thẻ sản phẩm nhỏ gọn).
- Ảnh chụp các màn hình chính: `docs/screenshots/` (trang chủ, menu, chi tiết món,
  đăng nhập, quản trị, mobile).

---

## 7. Ánh xạ CSDL → mã nguồn

| Bảng / View | Entity | Repository |
|---|---|---|
| roles | `Role` | `RoleRepository` |
| users | `User` | `UserRepository` |
| customers | `Customer` | `CustomerRepository` |
| staff | `Staff` | `StaffRepository` |
| addresses | `Address` | `AddressRepository` |
| categories | `Category` | `CategoryRepository` |
| products | `Product` | `ProductRepository` |
| product_sizes | `ProductSize` | `ProductSizeRepository` |
| toppings | `Topping` | `ToppingRepository` |
| vouchers | `Voucher` | `VoucherRepository` |
| orders | `Order` | `OrderRepository` |
| order_items | `OrderItem` | `OrderItemRepository` |
| order_item_toppings | `OrderItemTopping` | (qua `OrderItem`) |
| payments | `Payment` | `PaymentRepository` |
| reviews | `Review` | `ReviewRepository` |
| ingredients | `Ingredient` | `IngredientRepository` |
| v_daily_revenue / v_top_products | — | `ReportRepository` (native query) |

---

## 8. Ghi chú kỹ thuật

- **Đăng nhập**: `ShopUserDetailsService` đọc `users.phone` + `users.password_hash`,
  quyền lấy từ `roles.code` → `ROLE_OWNER`, `ROLE_CUSTOMER`…
- **Mật khẩu**: BCrypt (`BCryptPasswordEncoder`). Không lưu mật khẩu thô.
- **Giỏ hàng**: đối tượng `Cart` để trong `HttpSession` (khoá `CART`), không lưu DB
  cho tới khi đặt hàng.
- **Giá**: `unit_price` = `base_price + price_delta` của size; `line_total` =
  (`unit_price` + topping) × số lượng — đúng như thiết kế trong script SQL.
- **Phí giao hàng**: 15.000đ, chỉ cộng khi chọn `DELIVERY`.
- **Voucher**: kiểm tra thời gian, lượt dùng, đơn tối thiểu, trần giảm; khi huỷ đơn thì
  hoàn lại lượt dùng.
- **Bảo mật**: CSRF bật (Thymeleaf tự chèn token), phân quyền theo URL trong `SecurityConfig`.
- Dữ liệu mẫu: 5 danh mục, 5 đồ uống, 4 size, 5 topping, 5 voucher, 5 đơn hàng, 5 khách.

---

## 9. Lỗi thường gặp

| Hiện tượng | Cách xử lý |
|---|---|
| `Communications link failure` | MySQL chưa chạy — mở *Services* → `MySQL84` → Start |
| `Access denied for user 'root'` | Sai mật khẩu — sửa `spring.datasource.password` |
| `Unknown database 'drink_shop'` | Chưa chạy script: `mysql -u root -p < app_ban_nuoc.sql` |
| `Port 8080 was already in use` | Chạy `stop-port.bat` (tắt tiến trình giữ cổng), hoặc đổi cổng: `set SERVER_PORT=8081` rồi chạy lại |
| Đăng nhập báo sai mật khẩu | Khởi động lại app để `DataInitializer` băm lại mật khẩu demo |

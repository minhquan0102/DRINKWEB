-- =====================================================================
--  DATABASE: APP BÁN NƯỚC GIẢI KHÁT
--  Đối tượng: Khách hàng (tự order) | Nhân viên | Chủ quán
--  Kênh bán : Tại quầy (AT_COUNTER) + Online (ONLINE)
--  MySQL 8.0+ | utf8mb4
-- =====================================================================

DROP DATABASE IF EXISTS drink_shop;
CREATE DATABASE drink_shop CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE drink_shop;

-- =====================================================================
-- 1. PHÂN QUYỀN & NGƯỜI DÙNG
-- =====================================================================

CREATE TABLE roles (
    id          TINYINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(20)  NOT NULL UNIQUE,      -- OWNER, MANAGER, CASHIER, BARISTA, CUSTOMER
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(255) NULL
) ENGINE=InnoDB;

CREATE TABLE users (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    role_id       TINYINT UNSIGNED NOT NULL,
    full_name     VARCHAR(100) NOT NULL,
    phone         VARCHAR(15)  NOT NULL UNIQUE,
    email         VARCHAR(120) NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,           -- băm bằng bcrypt/argon2 ở phía app
    status        ENUM('ACTIVE','LOCKED','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB;

-- Thông tin riêng của khách hàng
CREATE TABLE customers (
    user_id        INT UNSIGNED PRIMARY KEY,
    loyalty_points INT UNSIGNED NOT NULL DEFAULT 0,
    birthday       DATE NULL,
    CONSTRAINT fk_customers_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Thông tin riêng của nhân viên / chủ
CREATE TABLE staff (
    user_id    INT UNSIGNED PRIMARY KEY,
    position   VARCHAR(50)   NOT NULL,
    hire_date  DATE          NOT NULL,
    salary     DECIMAL(12,0) NOT NULL DEFAULT 0,
    shift      ENUM('SANG','CHIEU','TOI','FULL') NOT NULL DEFAULT 'FULL',
    CONSTRAINT fk_staff_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- Địa chỉ giao hàng (order online)
CREATE TABLE addresses (
    id             INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    customer_id    INT UNSIGNED NOT NULL,
    receiver_name  VARCHAR(100) NOT NULL,
    receiver_phone VARCHAR(15)  NOT NULL,
    address_line   VARCHAR(255) NOT NULL,
    ward           VARCHAR(100) NULL,
    district       VARCHAR(100) NULL,
    city           VARCHAR(100) NOT NULL,
    is_default     TINYINT(1)   NOT NULL DEFAULT 0,
    CONSTRAINT fk_addr_customer FOREIGN KEY (customer_id) REFERENCES customers(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- =====================================================================
-- 2. MENU
-- =====================================================================

CREATE TABLE categories (
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    sort_order  INT NOT NULL DEFAULT 0,
    is_active   TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

CREATE TABLE products (
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    category_id  INT UNSIGNED NOT NULL,
    name         VARCHAR(150) NOT NULL,
    description  VARCHAR(500) NULL,
    base_price   DECIMAL(10,0) NOT NULL,           -- giá size M
    image_url    VARCHAR(255) NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1,    -- còn bán / hết hàng
    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_products_category (category_id)
) ENGINE=InnoDB;

CREATE TABLE product_sizes (
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(30) NOT NULL UNIQUE,
    price_delta DECIMAL(10,0) NOT NULL DEFAULT 0   -- cộng/trừ so với base_price
) ENGINE=InnoDB;

CREATE TABLE toppings (
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    price        DECIMAL(10,0) NOT NULL,
    is_available TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- =====================================================================
-- 3. KHUYẾN MÃI
-- =====================================================================

CREATE TABLE vouchers (
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(30) NOT NULL UNIQUE,
    description      VARCHAR(255) NULL,
    discount_type    ENUM('PERCENT','FIXED') NOT NULL,
    discount_value   DECIMAL(10,0) NOT NULL,       -- % hoặc số tiền
    max_discount     DECIMAL(10,0) NULL,           -- trần giảm (cho PERCENT)
    min_order_amount DECIMAL(10,0) NOT NULL DEFAULT 0,
    start_date       DATETIME NOT NULL,
    end_date         DATETIME NOT NULL,
    usage_limit      INT UNSIGNED NULL,
    used_count       INT UNSIGNED NOT NULL DEFAULT 0,
    is_active        TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB;

-- =====================================================================
-- 4. ĐƠN HÀNG
-- =====================================================================

CREATE TABLE orders (
    id              INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_code      VARCHAR(20) NOT NULL UNIQUE,
    order_type      ENUM('AT_COUNTER','ONLINE') NOT NULL,          -- kênh bán
    placed_by       ENUM('CUSTOMER','STAFF') NOT NULL,             -- khách tự order hay nhân viên nhập
    fulfillment     ENUM('DINE_IN','TAKEAWAY','DELIVERY') NOT NULL,
    customer_id     INT UNSIGNED NULL,                             -- NULL = khách vãng lai
    staff_id        INT UNSIGNED NULL,                             -- nhân viên xử lý
    address_id      INT UNSIGNED NULL,                             -- chỉ dùng khi DELIVERY
    table_no        VARCHAR(10) NULL,
    voucher_id      INT UNSIGNED NULL,
    status          ENUM('PENDING','CONFIRMED','PREPARING','READY','DELIVERING','COMPLETED','CANCELLED')
                    NOT NULL DEFAULT 'PENDING',
    subtotal        DECIMAL(12,0) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(12,0) NOT NULL DEFAULT 0,
    delivery_fee    DECIMAL(12,0) NOT NULL DEFAULT 0,
    total_amount    DECIMAL(12,0) NOT NULL DEFAULT 0,
    note            VARCHAR(255) NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customers(user_id),
    CONSTRAINT fk_orders_staff    FOREIGN KEY (staff_id)    REFERENCES staff(user_id),
    CONSTRAINT fk_orders_address  FOREIGN KEY (address_id)  REFERENCES addresses(id),
    CONSTRAINT fk_orders_voucher  FOREIGN KEY (voucher_id)  REFERENCES vouchers(id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created (created_at),
    INDEX idx_orders_customer (customer_id)
) ENGINE=InnoDB;

CREATE TABLE order_items (
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id    INT UNSIGNED NOT NULL,
    product_id  INT UNSIGNED NOT NULL,
    size_id     INT UNSIGNED NOT NULL,
    quantity    SMALLINT UNSIGNED NOT NULL DEFAULT 1,
    unit_price  DECIMAL(10,0) NOT NULL,            -- base_price + price_delta (chốt tại thời điểm bán)
    sugar_level ENUM('0%','30%','50%','70%','100%') NOT NULL DEFAULT '100%',
    ice_level   ENUM('0%','30%','50%','70%','100%') NOT NULL DEFAULT '100%',
    line_total  DECIMAL(12,0) NOT NULL,            -- (unit_price + topping) * quantity
    note        VARCHAR(255) NULL,
    CONSTRAINT fk_oi_order   FOREIGN KEY (order_id)   REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_oi_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_oi_size    FOREIGN KEY (size_id)    REFERENCES product_sizes(id),
    INDEX idx_oi_order (order_id)
) ENGINE=InnoDB;

CREATE TABLE order_item_toppings (
    id            INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_item_id INT UNSIGNED NOT NULL,
    topping_id    INT UNSIGNED NOT NULL,
    price         DECIMAL(10,0) NOT NULL,          -- giá topping chốt lúc bán
    CONSTRAINT fk_oit_item    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    CONSTRAINT fk_oit_topping FOREIGN KEY (topping_id)    REFERENCES toppings(id)
) ENGINE=InnoDB;

CREATE TABLE payments (
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id         INT UNSIGNED NOT NULL,
    method           ENUM('CASH','MOMO','VNPAY','BANK_TRANSFER','COD') NOT NULL,
    amount           DECIMAL(12,0) NOT NULL,
    status           ENUM('PENDING','PAID','FAILED','REFUNDED') NOT NULL DEFAULT 'PENDING',
    transaction_code VARCHAR(60) NULL,
    paid_at          DATETIME NULL,
    CONSTRAINT fk_pay_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_pay_status (status)
) ENGINE=InnoDB;

CREATE TABLE reviews (
    id          INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    order_id    INT UNSIGNED NOT NULL,
    product_id  INT UNSIGNED NOT NULL,
    customer_id INT UNSIGNED NOT NULL,
    rating      TINYINT UNSIGNED NOT NULL,
    comment     VARCHAR(500) NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rating CHECK (rating BETWEEN 1 AND 5),
    CONSTRAINT uq_review UNIQUE (order_id, product_id),
    CONSTRAINT fk_rv_order    FOREIGN KEY (order_id)    REFERENCES orders(id),
    CONSTRAINT fk_rv_product  FOREIGN KEY (product_id)  REFERENCES products(id),
    CONSTRAINT fk_rv_customer FOREIGN KEY (customer_id) REFERENCES customers(user_id)
) ENGINE=InnoDB;

-- =====================================================================
-- 5. KHO NGUYÊN LIỆU (chủ quán / quản lý theo dõi)
-- =====================================================================

CREATE TABLE ingredients (
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    unit       VARCHAR(20)  NOT NULL,
    stock_qty  DECIMAL(10,2) NOT NULL DEFAULT 0,
    min_qty    DECIMAL(10,2) NOT NULL DEFAULT 0,   -- dưới mức này thì cảnh báo nhập hàng
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- =====================================================================
-- DỮ LIỆU MẪU (5 dòng mỗi bảng, riêng users 10 dòng = 5 nhân sự + 5 khách)
-- Lưu ý: password_hash bên dưới chỉ là chuỗi giả, app phải tự băm mật khẩu thật.
-- =====================================================================

INSERT INTO roles (code, name, description) VALUES
('OWNER',    'Chủ quán',   'Toàn quyền, xem báo cáo doanh thu, quản lý nhân viên'),
('MANAGER',  'Quản lý',    'Quản lý menu, kho, voucher, ca làm'),
('CASHIER',  'Thu ngân',   'Nhận order tại quầy, thanh toán'),
('BARISTA',  'Pha chế',    'Nhận đơn và pha chế'),
('CUSTOMER', 'Khách hàng', 'Tự order tại quầy hoặc online');

INSERT INTO users (id, role_id, full_name, phone, email, password_hash, status) VALUES
(1,  1, 'Nguyễn Văn An',   '0901000001', 'an.owner@example.com',     '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(2,  2, 'Trần Thị Bình',   '0901000002', 'binh.manager@example.com', '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(3,  3, 'Lê Minh Châu',    '0901000003', 'chau.cashier@example.com', '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(4,  4, 'Phạm Quốc Dũng',  '0901000004', 'dung.barista@example.com', '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(5,  3, 'Hoàng Thị Em',    '0901000005', 'em.cashier@example.com',   '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(6,  5, 'Võ Thanh Phương', '0912000001', 'phuong@example.com',       '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(7,  5, 'Đặng Hữu Giang',  '0912000002', 'giang@example.com',        '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(8,  5, 'Bùi Ngọc Hà',     '0912000003', 'ha@example.com',           '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(9,  5, 'Đỗ Quang Huy',    '0912000004', 'huy@example.com',          '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE'),
(10, 5, 'Ngô Mai Khanh',   '0912000005', 'khanh@example.com',        '$2y$10$FAKE_HASH_REPLACE_IN_APP', 'ACTIVE');

INSERT INTO customers (user_id, loyalty_points, birthday) VALUES
(6,  120, '1998-03-12'),
(7,   45, '1995-07-25'),
(8,  300, '2001-11-02'),
(9,   10, '1990-01-18'),
(10,  80, '2003-09-30');

INSERT INTO staff (user_id, position, hire_date, salary, shift) VALUES
(1, 'Chủ quán',   '2023-01-01', 0,        'FULL'),
(2, 'Quản lý',    '2023-02-15', 12000000, 'FULL'),
(3, 'Thu ngân',   '2024-03-01',  8000000, 'SANG'),
(4, 'Pha chế',    '2024-05-10',  9000000, 'CHIEU'),
(5, 'Thu ngân',   '2025-01-20',  8000000, 'TOI');

INSERT INTO addresses (customer_id, receiver_name, receiver_phone, address_line, ward, district, city, is_default) VALUES
(6,  'Võ Thanh Phương', '0912000001', '12 Nguyễn Huệ',        'Bến Nghé',    'Quận 1',       'TP. Hồ Chí Minh', 1),
(7,  'Đặng Hữu Giang',  '0912000002', '45 Lê Văn Việt',       'Hiệp Phú',    'TP. Thủ Đức',  'TP. Hồ Chí Minh', 1),
(8,  'Bùi Ngọc Hà',     '0912000003', '78 Cách Mạng Tháng 8', 'Phường 10',   'Quận 3',       'TP. Hồ Chí Minh', 1),
(9,  'Đỗ Quang Huy',    '0912000004', '5 Phan Xích Long',     'Phường 2',    'Phú Nhuận',    'TP. Hồ Chí Minh', 1),
(10, 'Ngô Mai Khanh',   '0912000005', '99 Quang Trung',       'Phường 11',   'Gò Vấp',       'TP. Hồ Chí Minh', 1);

INSERT INTO categories (id, name, sort_order) VALUES
(1, 'Trà sữa',            1),
(2, 'Cà phê',             2),
(3, 'Trà trái cây',       3),
(4, 'Sinh tố & Nước ép',  4),
(5, 'Đá xay',             5);

INSERT INTO products (id, category_id, name, description, base_price, is_available) VALUES
(1, 1, 'Trà sữa trân châu đường đen', 'Trà sữa đậm vị, trân châu đường đen nấu tươi', 35000, 1),
(2, 2, 'Cà phê sữa đá',               'Cà phê phin truyền thống pha sữa đặc',          29000, 1),
(3, 3, 'Trà đào cam sả',              'Trà đen, đào miếng, cam vàng và sả',            39000, 1),
(4, 4, 'Sinh tố bơ',                  'Bơ sáp xay cùng sữa đặc',                       45000, 1),
(5, 5, 'Matcha đá xay',               'Matcha Nhật xay cùng đá và kem sữa',            49000, 1);

INSERT INTO product_sizes (id, name, price_delta) VALUES
(1, 'Size S',   -5000),
(2, 'Size M',        0),
(3, 'Size L',     6000),
(4, 'Size XL',   10000),
(5, 'Size Mini', -8000);

INSERT INTO toppings (id, name, price) VALUES
(1, 'Trân châu đen',   5000),
(2, 'Thạch dừa',       5000),
(3, 'Pudding trứng',   7000),
(4, 'Kem cheese',     10000),
(5, 'Thạch trái cây',  6000);

INSERT INTO vouchers (id, code, description, discount_type, discount_value, max_discount, min_order_amount, start_date, end_date, usage_limit, used_count) VALUES
(1, 'WELCOME10', 'Giảm 10% cho khách mới (tối đa 20k)',  'PERCENT', 10, 20000,  50000, '2026-11-01 00:00:00', '2026-12-31 23:59:59', 1000, 1),
(2, 'FREESHIP',  'Hỗ trợ 15k phí giao hàng',             'FIXED',   15000, NULL, 100000, '2026-11-01 00:00:00', '2026-12-31 23:59:59',  500, 0),
(3, 'GIAM5K',    'Giảm 5.000đ mọi đơn từ 60k',           'FIXED',    5000, NULL,  60000, '2026-11-01 00:00:00', '2026-11-30 23:59:59', 2000, 1),
(4, 'WEEKEND15', 'Cuối tuần giảm 15% (tối đa 30k)',      'PERCENT', 15, 30000, 100000, '2026-11-01 00:00:00', '2026-12-31 23:59:59',  300, 1),
(5, 'VIP20',     'Khách thân thiết giảm 20% (tối đa 50k)','PERCENT', 20, 50000, 150000, '2026-11-01 00:00:00', '2027-03-31 23:59:59',  100, 0);

-- Đơn 1: ONLINE, giao hàng, khách tự đặt, dùng WELCOME10
-- Đơn 2: TẠI QUẦY, nhân viên nhập, khách vãng lai, uống tại chỗ
-- Đơn 3: TẠI QUẦY, khách tự order (kiosk/app), mang đi, dùng GIAM5K
-- Đơn 4: ONLINE, tự đến lấy, khách tự đặt
-- Đơn 5: ONLINE, giao hàng, COD, dùng WEEKEND15 (đang chờ xác nhận)
INSERT INTO orders (id, order_code, order_type, placed_by, fulfillment, customer_id, staff_id, address_id, table_no, voucher_id, status, subtotal, discount_amount, delivery_fee, total_amount, note, created_at) VALUES
(1, 'DH20261114001', 'ONLINE',     'CUSTOMER', 'DELIVERY', 6,    3, 1,    NULL, 1,    'COMPLETED', 132000, 13200, 15000, 133800, 'Giao giờ hành chính',  '2026-11-14 09:15:00'),
(2, 'DH20261114002', 'AT_COUNTER', 'STAFF',    'DINE_IN',  NULL, 3, NULL, 'B3', NULL, 'COMPLETED',  58000,     0,     0,  58000, NULL,                   '2026-11-14 10:05:00'),
(3, 'DH20261115001', 'AT_COUNTER', 'CUSTOMER', 'TAKEAWAY', 7,    5, NULL, NULL, 3,    'COMPLETED', 110000,  5000,     0, 105000, 'Ít đá',               '2026-11-15 19:30:00'),
(4, 'DH20261115002', 'ONLINE',     'CUSTOMER', 'TAKEAWAY', 8,    5, NULL, NULL, NULL, 'COMPLETED',  94000,     0,     0,  94000, 'Hẹn 20:00 đến lấy',   '2026-11-15 19:45:00'),
(5, 'DH20261116001', 'ONLINE',     'CUSTOMER', 'DELIVERY', 9,    NULL, 4,   NULL, 4,    'PENDING',   135000, 20250, 15000, 129750, 'Gọi trước khi giao',  '2026-11-16 08:50:00');

INSERT INTO order_items (id, order_id, product_id, size_id, quantity, unit_price, sugar_level, ice_level, line_total, note) VALUES
(1, 1, 1, 2, 2, 35000, '70%',  '50%',  80000, NULL),
(2, 1, 3, 3, 1, 45000, '50%',  '100%', 52000, NULL),
(3, 2, 2, 2, 2, 29000, '100%', '100%', 58000, NULL),
(4, 3, 4, 3, 1, 51000, '100%', '30%',  51000, NULL),
(5, 3, 5, 2, 1, 49000, '70%',  '100%', 59000, NULL),
(6, 4, 1, 1, 1, 30000, '50%',  '50%',  35000, NULL),
(7, 4, 5, 4, 1, 59000, '100%', '100%', 59000, NULL),
(8, 5, 3, 2, 2, 39000, '70%',  '70%',  90000, NULL),
(9, 5, 4, 2, 1, 45000, '100%', '50%',  45000, NULL);

INSERT INTO order_item_toppings (order_item_id, topping_id, price) VALUES
(1, 1,  5000),   -- đơn 1: trà sữa + trân châu đen (mỗi ly)
(2, 3,  7000),   -- đơn 1: trà đào + pudding
(5, 4, 10000),   -- đơn 3: matcha + kem cheese
(6, 2,  5000),   -- đơn 4: trà sữa size S + thạch dừa
(8, 5,  6000);   -- đơn 5: trà đào + thạch trái cây (mỗi ly)

INSERT INTO payments (id, order_id, method, amount, status, transaction_code, paid_at) VALUES
(1, 1, 'VNPAY',         133800, 'PAID',    'VNP20261114001', '2026-11-14 09:16:10'),
(2, 2, 'CASH',           58000, 'PAID',    NULL,             '2026-11-14 10:06:00'),
(3, 3, 'MOMO',          105000, 'PAID',    'MOMO20261115001','2026-11-15 19:31:20'),
(4, 4, 'BANK_TRANSFER',  94000, 'PAID',    'FT26111500123',  '2026-11-15 19:47:00'),
(5, 5, 'COD',           129750, 'PENDING', NULL,             NULL);

INSERT INTO reviews (order_id, product_id, customer_id, rating, comment) VALUES
(1, 1, 6, 5, 'Trân châu dẻo, đường đen thơm'),
(1, 3, 6, 4, 'Trà thơm, hơi ngọt với mình'),
(3, 4, 7, 5, 'Bơ béo, đậm vị'),
(3, 5, 7, 4, 'Matcha ngon, kem cheese hơi nhiều'),
(4, 5, 8, 5, 'Rất ưng, đá xay mịn');

INSERT INTO ingredients (name, unit, stock_qty, min_qty) VALUES
('Trà đen',      'kg',   8.50,  3.00),
('Sữa tươi',     'lít', 25.00, 10.00),
('Cà phê hạt',   'kg',  12.00,  5.00),
('Trân châu',    'kg',   2.00,  3.00),   -- dưới mức tối thiểu -> cần nhập thêm
('Bột matcha',   'kg',   1.50,  1.00);

-- =====================================================================
-- VIEW BÁO CÁO CHO CHỦ QUÁN
-- =====================================================================

CREATE VIEW v_daily_revenue AS
SELECT DATE(o.created_at) AS sale_date,
       o.order_type,
       COUNT(*)           AS total_orders,
       SUM(o.total_amount) AS revenue
FROM orders o
WHERE o.status = 'COMPLETED'
GROUP BY DATE(o.created_at), o.order_type;

CREATE VIEW v_top_products AS
SELECT p.id, p.name,
       SUM(oi.quantity)   AS qty_sold,
       SUM(oi.line_total) AS revenue
FROM order_items oi
JOIN orders   o ON o.id = oi.order_id AND o.status = 'COMPLETED'
JOIN products p ON p.id = oi.product_id
GROUP BY p.id, p.name
ORDER BY qty_sold DESC;

CREATE VIEW v_low_stock AS
SELECT id, name, unit, stock_qty, min_qty
FROM ingredients
WHERE stock_qty < min_qty;

-- =====================================================================
-- MỘT SỐ TRUY VẤN THƯỜNG DÙNG
-- =====================================================================

-- Doanh thu theo ngày & kênh bán (chủ quán)
-- SELECT * FROM v_daily_revenue ORDER BY sale_date DESC;

-- Món bán chạy
-- SELECT * FROM v_top_products LIMIT 5;

-- Nguyên liệu sắp hết
-- SELECT * FROM v_low_stock;

-- Đơn đang chờ pha chế (màn hình nhân viên)
-- SELECT order_code, order_type, fulfillment, table_no, created_at
-- FROM orders WHERE status IN ('PENDING','CONFIRMED','PREPARING') ORDER BY created_at;

-- Chi tiết 1 đơn (hóa đơn)
-- SELECT o.order_code, p.name, ps.name AS size, oi.quantity, oi.unit_price,
--        GROUP_CONCAT(t.name SEPARATOR ', ') AS toppings, oi.line_total
-- FROM orders o
-- JOIN order_items oi        ON oi.order_id = o.id
-- JOIN products p            ON p.id = oi.product_id
-- JOIN product_sizes ps      ON ps.id = oi.size_id
-- LEFT JOIN order_item_toppings oit ON oit.order_item_id = oi.id
-- LEFT JOIN toppings t       ON t.id = oit.topping_id
-- WHERE o.order_code = 'DH20261114001'
-- GROUP BY oi.id;

-- Lịch sử đơn của 1 khách
-- SELECT order_code, order_type, status, total_amount, created_at
-- FROM orders WHERE customer_id = 6 ORDER BY created_at DESC;

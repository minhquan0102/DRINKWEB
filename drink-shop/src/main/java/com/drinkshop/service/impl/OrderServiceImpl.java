package com.drinkshop.service.impl;

import com.drinkshop.model.Address;
import com.drinkshop.model.Customer;
import com.drinkshop.model.Fulfillment;
import com.drinkshop.model.Order;
import com.drinkshop.model.OrderItem;
import com.drinkshop.model.OrderItemTopping;
import com.drinkshop.model.OrderStatus;
import com.drinkshop.model.OrderType;
import com.drinkshop.model.Payment;
import com.drinkshop.model.PaymentMethod;
import com.drinkshop.model.PaymentStatus;
import com.drinkshop.model.PlacedBy;
import com.drinkshop.model.Product;
import com.drinkshop.model.ProductSize;
import com.drinkshop.model.Staff;
import com.drinkshop.model.Topping;
import com.drinkshop.model.User;
import com.drinkshop.model.Voucher;
import com.drinkshop.model.dto.Cart;
import com.drinkshop.model.dto.CartItem;
import com.drinkshop.model.dto.CheckoutForm;
import com.drinkshop.model.dto.VoucherResult;
import com.drinkshop.repository.AddressRepository;
import com.drinkshop.repository.CustomerRepository;
import com.drinkshop.repository.OrderRepository;
import com.drinkshop.repository.PaymentRepository;
import com.drinkshop.repository.ProductRepository;
import com.drinkshop.repository.ProductSizeRepository;
import com.drinkshop.repository.StaffRepository;
import com.drinkshop.repository.ToppingRepository;
import com.drinkshop.repository.UserRepository;
import com.drinkshop.repository.VoucherRepository;
import com.drinkshop.service.OrderService;
import com.drinkshop.service.VoucherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    /** Phí giao hàng cố định cho đơn DELIVERY. */
    public static final BigDecimal DELIVERY_FEE = BigDecimal.valueOf(15000);

    private static final DateTimeFormatter CODE_FORMAT = DateTimeFormatter.ofPattern("yyMMddHHmmss");

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductSizeRepository productSizeRepository;
    private final ToppingRepository toppingRepository;
    private final VoucherRepository voucherRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final VoucherService voucherService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            ProductSizeRepository productSizeRepository,
                            ToppingRepository toppingRepository,
                            VoucherRepository voucherRepository,
                            PaymentRepository paymentRepository,
                            CustomerRepository customerRepository,
                            StaffRepository staffRepository,
                            UserRepository userRepository,
                            AddressRepository addressRepository,
                            VoucherService voucherService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productSizeRepository = productSizeRepository;
        this.toppingRepository = toppingRepository;
        this.voucherRepository = voucherRepository;
        this.paymentRepository = paymentRepository;
        this.customerRepository = customerRepository;
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.voucherService = voucherService;
    }

    @Override
    public Order placeOrder(Integer userId, Cart cart, CheckoutForm form) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống.");
        }

        User user = userId == null ? null : userRepository.findById(userId).orElse(null);
        Customer customer = null;
        Staff staff = null;
        if (user != null) {
            if (user.isStaff()) {
                staff = staffRepository.findByUser_Id(user.getId()).orElse(null);
            } else {
                customer = customerRepository.findByUser_Id(user.getId()).orElse(null);
            }
        }

        OrderType orderType = form.getOrderType() != null ? form.getOrderType() : OrderType.ONLINE;
        Fulfillment fulfillment = form.getFulfillment() != null ? form.getFulfillment() : Fulfillment.TAKEAWAY;

        Address address = null;
        BigDecimal deliveryFee = BigDecimal.ZERO;
        if (fulfillment == Fulfillment.DELIVERY) {
            if (form.getAddressId() == null) {
                throw new IllegalStateException("Vui lòng chọn địa chỉ giao hàng.");
            }
            address = addressRepository.findById(form.getAddressId())
                    .orElseThrow(() -> new IllegalStateException("Địa chỉ giao hàng không hợp lệ."));
            if (customer != null && address.getCustomer() != null
                    && !address.getCustomer().getUserId().equals(customer.getUserId())) {
                throw new IllegalStateException("Địa chỉ không thuộc tài khoản của bạn.");
            }
            deliveryFee = DELIVERY_FEE;
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(orderType);
        order.setPlacedBy(customer != null ? PlacedBy.CUSTOMER : PlacedBy.STAFF);
        order.setFulfillment(fulfillment);
        order.setCustomer(customer);
        order.setStaff(staff);
        order.setAddress(address);
        order.setTableNo(fulfillment == Fulfillment.DINE_IN ? truncate(form.getTableNo(), 10) : null);
        order.setNote(truncate(form.getNote(), 255));
        order.setStatus(OrderStatus.PENDING);

        BigDecimal subtotal = BigDecimal.ZERO;
        for (CartItem line : cart.getItems()) {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Món \"" + line.getProductName() + "\" không còn trong menu."));
            ProductSize size = productSizeRepository.findById(line.getSizeId())
                    .orElseThrow(() -> new IllegalStateException("Size của món không hợp lệ."));

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setSize(size);
            item.setQuantity(line.getQuantity());
            item.setSugarLevel(line.getSugarLevel());
            item.setIceLevel(line.getIceLevel());
            item.setNote(truncate(line.getNote(), 255));

            BigDecimal unitPrice = product.getBasePrice().add(size.getPriceDelta());
            item.setUnitPrice(unitPrice);

            BigDecimal toppingSum = BigDecimal.ZERO;
            for (Integer toppingId : line.getToppingIds()) {
                Topping topping = toppingRepository.findById(toppingId).orElse(null);
                if (topping == null) {
                    continue;
                }
                item.addTopping(new OrderItemTopping(topping, topping.getPrice()));
                toppingSum = toppingSum.add(topping.getPrice());
            }

            BigDecimal lineTotal = unitPrice.add(toppingSum).multiply(BigDecimal.valueOf(line.getQuantity()));
            item.setLineTotal(lineTotal);
            subtotal = subtotal.add(lineTotal);
            order.addItem(item);
        }

        order.setSubtotal(subtotal);

        BigDecimal discount = BigDecimal.ZERO;
        if (form.getVoucherCode() != null && !form.getVoucherCode().isBlank()) {
            VoucherResult result = voucherService.validate(form.getVoucherCode(), subtotal);
            if (!result.valid()) {
                throw new IllegalStateException(result.message());
            }
            discount = result.discount();
            Voucher voucher = voucherRepository.findById(result.voucherId()).orElse(null);
            if (voucher != null) {
                voucher.setUsedCount(voucher.getUsedCount() + 1);
                voucherRepository.save(voucher);
                order.setVoucher(voucher);
            }
        }

        BigDecimal total = subtotal.subtract(discount).add(deliveryFee);
        if (total.signum() < 0) {
            total = BigDecimal.ZERO;
        }
        order.setDiscountAmount(discount);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(total);

        Order saved = orderRepository.save(order);

        PaymentMethod method = form.getPaymentMethod() != null ? form.getPaymentMethod() : PaymentMethod.CASH;
        Payment payment = new Payment();
        payment.setOrder(saved);
        payment.setMethod(method);
        payment.setAmount(total);
        if (method == PaymentMethod.CASH) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setPaidAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.PENDING);
        }
        paymentRepository.save(payment);

        cart.clear();
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Order getById(Integer id) {
        return orderRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public Order getByCode(String code) {
        return orderRepository.findByOrderCode(code).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> ordersOfCustomer(Integer userId) {
        return orderRepository.findByCustomer_UserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> allOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> activeOrders() {
        return orderRepository.findByStatusInOrderByCreatedAtAsc(
                List.of(OrderStatus.PENDING, OrderStatus.CONFIRMED, OrderStatus.PREPARING,
                        OrderStatus.READY, OrderStatus.DELIVERING));
    }

    @Override
    public void updateStatus(Integer orderId, OrderStatus newStatus, Integer staffUserId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Đơn hàng không tồn tại."));

        if (!nextStatuses(order.getStatus()).contains(newStatus)) {
            throw new IllegalStateException("Không thể chuyển đơn từ \""
                    + order.getStatus().getLabel() + "\" sang \"" + newStatus.getLabel() + "\".");
        }

        order.setStatus(newStatus);
        if (staffUserId != null && order.getStaff() == null) {
            staffRepository.findByUser_Id(staffUserId).ifPresent(order::setStaff);
        }

        Payment payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);

        if (newStatus == OrderStatus.COMPLETED) {
            if (payment != null && payment.getStatus() == PaymentStatus.PENDING) {
                payment.setStatus(PaymentStatus.PAID);
                payment.setPaidAt(LocalDateTime.now());
                paymentRepository.save(payment);
            }
            if (order.getCustomer() != null) {
                Customer customer = order.getCustomer();
                int points = order.getTotalAmount()
                        .divide(BigDecimal.valueOf(10000), 0, RoundingMode.DOWN)
                        .intValue();
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() + points);
                customerRepository.save(customer);
            }
        }

        if (newStatus == OrderStatus.CANCELLED) {
            if (payment != null && payment.getStatus() == PaymentStatus.PAID) {
                payment.setStatus(PaymentStatus.REFUNDED);
                paymentRepository.save(payment);
            }
            if (order.getVoucher() != null) {
                Voucher voucher = order.getVoucher();
                voucher.setUsedCount(Math.max(0, voucher.getUsedCount() - 1));
                voucherRepository.save(voucher);
            }
        }

        orderRepository.save(order);
    }

    @Override
    public List<OrderStatus> nextStatuses(OrderStatus current) {
        if (current == null) {
            return List.of();
        }
        return switch (current) {
            case PENDING -> List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED);
            case CONFIRMED -> List.of(OrderStatus.PREPARING, OrderStatus.CANCELLED);
            case PREPARING -> List.of(OrderStatus.READY, OrderStatus.CANCELLED);
            case READY -> List.of(OrderStatus.DELIVERING, OrderStatus.COMPLETED);
            case DELIVERING -> List.of(OrderStatus.COMPLETED);
            case COMPLETED, CANCELLED -> List.of();
        };
    }

    @Override
    @Transactional(readOnly = true)
    public Payment getPayment(Integer orderId) {
        return paymentRepository.findByOrder_Id(orderId).orElse(null);
    }

    @Override
    public Payment markPaid(String orderCode, String transactionCode, String methodNote) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalStateException("Đơn hàng không tồn tại: " + orderCode));
        Payment payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);
        if (payment == null) {
            return null;
        }
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(LocalDateTime.now());
        payment.setTransactionCode(transactionCode);
        if (methodNote != null && !methodNote.isBlank()) {
            payment.setTransactionCode(transactionCode + " · " + methodNote);
        }
        paymentRepository.save(payment);
        return payment;
    }

    @Override
    public Payment markFailed(String orderCode, String responseCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalStateException("Đơn hàng không tồn tại: " + orderCode));
        Payment payment = paymentRepository.findByOrder_Id(order.getId()).orElse(null);
        if (payment == null || payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }
        payment.setStatus(PaymentStatus.FAILED);
        payment.setTransactionCode(responseCode == null ? null : "FAILED-" + responseCode);
        paymentRepository.save(payment);
        return payment;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    /** Sinh mã đơn dạng DH + yyMMddHHmmss + 2 số ngẫu nhiên. */
    private String generateOrderCode() {
        String base = "DH" + LocalDateTime.now().format(CODE_FORMAT);
        for (int i = 0; i < 20; i++) {
            String code = base + (10 + (int) (Math.random() * 90));
            if (orderRepository.findByOrderCode(code).isEmpty()) {
                return code;
            }
        }
        return base + (System.nanoTime() % 90 + 10);
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}

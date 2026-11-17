package com.drinkshop.repository;

import com.drinkshop.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    Optional<Payment> findByOrder_Id(Integer orderId);

    List<Payment> findByOrder_OrderCode(String orderCode);

    /** Số giao dịch đã thanh toán theo từng phương thức — dùng cho biểu đồ. */
    @Query(value = "SELECT method, COUNT(*) FROM payments WHERE status = 'PAID' GROUP BY method", nativeQuery = true)
    List<Object[]> countPaidByMethod();
}

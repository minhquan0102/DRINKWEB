package com.drinkshop.repository;

import com.drinkshop.model.Order;
import com.drinkshop.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findAllByOrderByCreatedAtDesc();

    List<Order> findByCustomer_UserIdOrderByCreatedAtDesc(Integer customerId);

    List<Order> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    List<Order> findByStatusInOrderByCreatedAtAsc(List<OrderStatus> statuses);

    long countByStatus(OrderStatus status);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status = :status")
    BigDecimal sumTotalByStatus(@Param("status") OrderStatus status);

    @Query("select count(o) from Order o where o.createdAt >= :from")
    long countSince(@Param("from") LocalDateTime from);

    @Query("select coalesce(sum(o.totalAmount), 0) from Order o where o.status = :status and o.createdAt >= :from")
    BigDecimal sumTotalByStatusSince(@Param("status") OrderStatus status, @Param("from") LocalDateTime from);
}

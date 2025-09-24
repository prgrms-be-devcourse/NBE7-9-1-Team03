package com.coffee.domain.order.repository;

import com.coffee.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @Modifying
    @Query("UPDATE Order o SET o.orderState = true WHERE o.orderDate >= :startTime AND o.orderDate <= :endTime AND o.orderState = false")
    int updateOrderStateByDateRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    // 조회용 (배치 실행 전 확인용)
    List<Order> findByOrderDateBetweenAndOrderState(LocalDateTime startTime, LocalDateTime endTime, Boolean orderState);

    List<Order> findByCustomerEmail(String customerEmail);
}
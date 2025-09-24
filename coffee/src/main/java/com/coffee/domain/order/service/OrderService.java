package com.coffee.domain.order.service;

import com.coffee.domain.order.entity.Order;
import com.coffee.domain.order.repository.OrderRepository;
import com.coffee.domain.user.entity.User; // User import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본적으로 읽기 전용
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional // 쓰기 작업이므로 readOnly = false
    public Order createOrder(User user) {
        Order order = Order.builder()
                .userId(user.getUserId())
                .orderDay(LocalDateTime.now())
                .build();

        return orderRepository.save(order);
    }

    public long count() {
        return orderRepository.count();
    }

    public Optional<Order> findById(Long id) { // long -> Long 권장
        return orderRepository.findById(id);
    }

    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional // 수정 작업이므로 readOnly = false
    public void updateOrderState(Long orderId, Boolean state) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다"));

        order.setOrderState(state);
    }

    @Transactional
    public void delete(Order order) {
        orderRepository.delete(order);
    }

    public void flush() {
        orderRepository.flush();
    }
}
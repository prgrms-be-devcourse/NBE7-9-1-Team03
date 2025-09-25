package com.coffee.domain.order.service;

import com.coffee.domain.order.dto.OrderDto;
import com.coffee.domain.order.entity.Order;
import com.coffee.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본 읽기 전용
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public LocalDateTime createOrder(OrderDto dto) {
        Order order = Order.builder()
                .customerEmail(dto.getCustomerEmail())
                .productId(dto.getProductId())  // 추가
                .orderDate(LocalDateTime.now())
                .orderState(false)
                .quantity(dto.getQuantity())
                .build();

        orderRepository.save(order);
        // order ID는 단순히 내부 관리용이고 실제로는 고객 이메일과 주문 날짜로 조회할 것이므로 반환하지 않음
        return order.getOrderDate();
    }

    public List<OrderDto> findAllByCustomerEmail(String customerEmail) {
        List<Order> orders = orderRepository.findAllByCustomerEmail(customerEmail);

        if (orders.isEmpty()) {
            return List.of(); // 빈 리스트 반환
        }

        return orders.stream()
                .map(OrderDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public LocalDateTime updateOrder(OrderDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다"));

        if (dto.getQuantity() != null) {
            order.setQuantity(dto.getQuantity());
            order.setOrderDate(LocalDateTime.now());
        }
        if (dto.getOrderState() != null) {
            order.setOrderState(dto.getOrderState());
            order.setOrderDate(LocalDateTime.now());
        }
        //주문 수정이 일어난 경우 현재 시간을 주문 시간으로 설정하기 때문에 현재시각을 반환
        return LocalDateTime.now();
    }

    @Transactional
    public void deleteByCustomerEmail(String customerEmail) {
        orderRepository.deleteByCustomerEmail(customerEmail);
    }
}
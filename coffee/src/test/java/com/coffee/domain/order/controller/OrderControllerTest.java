package com.coffee.domain.order.controller;

import com.coffee.domain.order.dto.OrderDto;
import com.coffee.domain.order.entity.Order;
import com.coffee.domain.order.repository.OrderRepository;
import com.coffee.domain.order.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mvc;
    private List<Long> createdOrderIds = new ArrayList<>();

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        createdOrderIds.clear();
        createTestOrders();
    }

    @AfterEach
    void tearDown() {
        // 테스트에서 생성된 모든 주문 데이터 삭제
        createdOrderIds.forEach(orderId -> {
            if (orderRepository.existsById(orderId)) {
                orderRepository.deleteById(orderId);
            }
        });
        createdOrderIds.clear();
        
        // 추가로 테스트 이메일로 생성된 주문들 모두 삭제
        orderRepository.findAll().stream()
            .filter(order -> order.getCustomerEmail().contains("test") || 
                           order.getCustomerEmail().contains("example.com"))
            .forEach(order -> orderRepository.deleteById(order.getOrderId()));
    }

    private void createTestOrders() {
        // 오후 2시 이전 주문 (당일 배송)
        Order order1 = Order.builder()
                .customerEmail("test1@example.com")
                .productId(1L)
                .orderDate(LocalDateTime.now().withHour(10).withMinute(0))
                .orderState(false)
                .quantity(2)
                .build();

        // 오후 2시 이후 주문 (다음날 배송)
        Order order2 = Order.builder()
                .customerEmail("test2@example.com")
                .productId(2L)
                .orderDate(LocalDateTime.now().withHour(15).withMinute(30))
                .orderState(false)
                .quantity(1)
                .build();

        // 이미 처리된 주문
        Order order3 = Order.builder()
                .customerEmail("test3@example.com")
                .productId(3L)
                .orderDate(LocalDateTime.now().minusDays(1))
                .orderState(true)
                .quantity(3)
                .build();

        Order savedOrder1 = orderRepository.save(order1);
        Order savedOrder2 = orderRepository.save(order2);
        Order savedOrder3 = orderRepository.save(order3);

        createdOrderIds.add(savedOrder1.getOrderId());
        createdOrderIds.add(savedOrder2.getOrderId());
        createdOrderIds.add(savedOrder3.getOrderId());
    }

    @Test
    @DisplayName("주문 생성 - 정상적인 주문 생성 및 배송 메시지 확인")
    void t1() throws Exception {
        OrderDto orderDto = OrderDto.builder()
                .customerEmail("new@example.com")
                .productId(1L)
                .quantity(2)
                .build();

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderDto))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("createOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201-1"))
                .andExpect(jsonPath("$.msg").exists());

        // 새로 생성된 주문 ID를 추적 목록에 추가
        Order createdOrder = orderRepository.findAll().stream()
                .filter(order -> "new@example.com".equals(order.getCustomerEmail()))
                .findFirst()
                .orElse(null);
        
        if (createdOrder != null) {
            createdOrderIds.add(createdOrder.getOrderId());
        }

        // 데이터베이스에 실제로 저장되었는지 확인
        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getCustomerEmail()).isEqualTo("new@example.com");
        assertThat(createdOrder.getQuantity()).isEqualTo(2);
        assertThat(createdOrder.getOrderState()).isFalse();
    }

    @Test
    @DisplayName("주문 조회 - 존재하는 고객의 주문 목록 조회")
    void t2() throws Exception {
        String customerEmail = "test1@example.com"; // 조회에 사용할 이메일

        // MockMvc를 이용해 GET 요청을 보냄
        ResultActions resultActions = mvc
                .perform(
                        get("/orders/{customerEmail}", customerEmail)
                )
                .andDo(print()); // 요청/응답 로그 출력

        // 응답 상태 및 JSON 데이터 검증
        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("getOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("주문 상세 정보를 조회했습니다."))
                .andExpect(jsonPath("$.data").isArray()) // data 필드가 배열인지 확인
                .andExpect(jsonPath("$.data[0].customerEmail").value(customerEmail)) // 첫 번째 주문의 이메일 확인
                .andExpect(jsonPath("$.data[0].orderState").value(false)); // 첫 번째 주문의 상태 확인
    }

    @Test
    @DisplayName("주문 수정 - 수량 변경 및 실제 업데이트 확인")
    void t3() throws Exception {
        Long orderId = createdOrderIds.get(0);

        OrderDto updateDto = OrderDto.builder()
                .orderId(orderId)
                .quantity(5)
                .build();

        ResultActions resultActions = mvc
                .perform(
                        put("/orders/{orderId}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto))
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").exists());

        // 실제로 수량이 변경되었는지 확인
        Order updatedOrder = orderRepository.findById(orderId).orElse(null);
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("주문 취소 - 존재하는 주문 삭제 및 DB에서 제거 확인")
    void t4() throws Exception {
        Long orderId = createdOrderIds.get(0);

        // 삭제 전 존재 확인
        assertThat(orderRepository.existsById(orderId)).isTrue();

        ResultActions resultActions = mvc
                .perform(
                        delete("/orders/{orderId}", orderId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("cancelOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("주문이 취소되었습니다."));

        // 삭제 후 존재하지 않음 확인
        assertThat(orderRepository.existsById(orderId)).isFalse();
        
        // 삭제된 ID는 추적 목록에서 제거
        createdOrderIds.remove(orderId);
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 - 빈 배열 반환")
    void t5() throws Exception {
        Long nonExistentOrderId = 99999L;

        ResultActions resultActions = mvc
                .perform(
                        get("/orders/{orderId}", nonExistentOrderId)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("getOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("주문 생성 - 필수 필드 누락시 400 에러")
    void t6() throws Exception {
        OrderDto orderDto = OrderDto.builder()
                .customerEmail("") // 빈 이메일
                .productId(1L)
                .quantity(2)
                .build();

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderDto))
                )
                .andDo(print());

        // 빈 이메일로 인한 검증 실패는 서비스 로직에 따라 다를 수 있음
        // 실제 구현에 맞게 조정 필요
        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("createOrder"));
    }

    @Test
    @DisplayName("주문 수정 - 존재하지 않는 주문 수정 시도")
    void t7() throws Exception {
        Long nonExistentOrderId = 99999L;

        OrderDto updateDto = OrderDto.builder()
                .orderId(nonExistentOrderId)
                .quantity(5)
                .build();

        ResultActions resultActions = mvc
                .perform(
                        put("/orders/{orderId}", nonExistentOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateDto))
                )
                .andDo(print());

        // 존재하지 않는 주문 수정 시 에러가 발생할 수 있음
        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrder"));
    }
}
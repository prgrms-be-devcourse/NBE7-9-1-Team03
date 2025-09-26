package com.coffee.domain.order.controller;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.repository.CustomerRepository;
import com.coffee.domain.order.dto.OrderDto;
import com.coffee.domain.order.entity.Order;
import com.coffee.domain.order.repository.OrderRepository;
import com.coffee.domain.order.service.OrderService;
import com.coffee.domain.product.entity.Product;
import com.coffee.domain.product.repository.ProductRepository;
import com.coffee.global.globalExceptionHandler.GlobalExceptionHandler;
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
import java.util.stream.Collectors;

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

    // 💡 GlobalExceptionHandler 주입
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mvc;
    private List<Long> createdOrderIds = new ArrayList<>();

    // 테스트에서 사용할 고객 및 상품 데이터 목록
    private List<Customer> testCustomers = new ArrayList<>();
    private List<Product> testProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createTestCustomers();
        createTestProducts();

        // 💡 MockMvc 설정 보강: GlobalExceptionHandler를 명시적으로 등록
        mvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .build();

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

        orderRepository.deleteAll(); // 남아있는 주문 데이터 정리
        customerRepository.deleteAll(testCustomers);
        productRepository.deleteAll(testProducts);
    }

    private void createTestCustomers() {
        // OrderControllerTest에서 사용할 이메일 목록
        List<String> emails = List.of("test1@example.com", "test2@example.com", "test3@example.com", "new@example.com");

        emails.forEach(email -> {
            Customer customer = Customer.builder()
                    .email(email)
                    .password("password123")
                    .username("Test User " + email.split("@")[0])
                    .address("Test Address")
                    .postalCode(12345)
                    .build();
            testCustomers.add(customerRepository.save(customer));
        });
    }

    private void createTestProducts() {
        Product product1 = Product.builder().name("Test Product 1").price(10000).stock(10).build();
        Product product2 = Product.builder().name("Test Product 2").price(20000).stock(5).build();
        Product product3 = Product.builder().name("Test Product 3").price(30000).stock(20).build();

        testProducts.add(productRepository.save(product1));
        testProducts.add(productRepository.save(product2));
        testProducts.add(productRepository.save(product3));
    }

    private void createTestOrders() {

        Long productId1 = testProducts.get(0).getId();
        Long productId2 = testProducts.get(1).getId();
        Long productId3 = testProducts.get(2).getId();

        // 오후 2시 이전 주문 (당일 배송)
        Order order1 = Order.builder()
                .customerEmail("test1@example.com")
                .productId(productId1)
                .orderDate(LocalDateTime.now().withHour(10).withMinute(0))
                .orderState(false)
                .quantity(2)
                .build();

        // 오후 2시 이후 주문 (다음날 배송)
        Order order2 = Order.builder()
                .customerEmail("test2@example.com")
                .productId(productId2)
                .orderDate(LocalDateTime.now().withHour(15).withMinute(30))
                .orderState(false)
                .quantity(1)
                .build();

        // 이미 처리된 주문
        Order order3 = Order.builder()
                .customerEmail("test3@example.com")
                .productId(productId3)
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
        // 💡 List<OrderDto>로 감싸서 요청 본문을 생성
        List<OrderDto> orderList = List.of(
                OrderDto.builder()
                        .customerEmail("new@example.com")
                        .productId(testProducts.get(0).getId())
                        .quantity(2)
                        .build()
        );

        ResultActions resultActions = mvc
                .perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(orderList)) // List를 전송
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("createOrder"))
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
        String customerEmail = "test1@example.com";

        List<OrderDto> updateList = List.of(
                OrderDto.builder()
                        .orderId(orderId)
                        .customerEmail(customerEmail)
                        .quantity(5)
                        .build()
        );


        ResultActions resultActions = mvc
                .perform(
                        put("/orders/{orderId}", orderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateList)) // List를 전송
                )
                .andDo(print());

        String responseBody = resultActions.andReturn()
                .getResponse()
                .getContentAsString();

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrder"))
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").exists());

        // 실제로 수량이 변경되었는지 확인
        Order updatedOrder = orderRepository.findById(orderId).orElse(null);
        assertThat(updatedOrder).isNotNull();
        assertThat(updatedOrder.getQuantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("주문 취소 - 존재하는 고객의 모든 주문 삭제 및 DB에서 제거 확인")
    void t4() throws Exception {
        String email = "test1@example.com";

        // 💡 Order 엔티티에서 Order ID (Long) 목록을 직접 추출합니다.
        List<Long> orderIdsToRemove = orderRepository.findAllByCustomerEmail(email).stream()
                .map(Order::getOrderId)
                .collect(Collectors.toList());
        for(Long id : orderIdsToRemove) {
            System.out.println("삭제 대상 주문 ID: " + id);
        }
        // 삭제 전 존재 확인
        assertThat(orderIdsToRemove).isNotEmpty();

        // 이메일로 삭제하는 DELETE 요청
        ResultActions resultActions = mvc
                .perform(
                        delete("/orders/{customerEmail}", email)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("cancelOrder"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("주문이 취소되었습니다."));

        // 삭제 후 해당 이메일의 주문이 존재하지 않음 확인
        assertThat(orderRepository.findAllByCustomerEmail(email)).isEmpty();

        // 💡 createdOrderIds 목록에서 삭제된 ID들을 제거 (Long 객체 제거)
        // List의 removeAll 메서드를 사용하여 한 번에 처리하는 것이 효율적입니다.
        createdOrderIds.removeAll(orderIdsToRemove);

        // 선택 사항: createdOrderIds에 해당 ID가 남아있지 않은지 최종 확인
        orderIdsToRemove.forEach(id -> {
            assertThat(createdOrderIds).doesNotContain(id);
        });
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회 - 빈 배열 반환 (현재 OrderController는 email로 조회)")
    void t5() throws Exception {
        // t5는 현재 OrderController의 getOrder 경로(/orders/{customerEmail})에 맞게 email로 조회하도록 작성되어야 함
        String nonExistentEmail = "nonexistent@example.com";

        ResultActions resultActions = mvc
                .perform(
                        get("/orders/{customerEmail}", nonExistentEmail)
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
    @DisplayName("주문 수정 - 존재하지 않는 주문 수정 시도 (t7)")
    void t6() throws Exception {
        Long nonExistentOrderId = 99999L;
        String customerEmail = "test1@example.com";

        // 💡 List<OrderDto>로 감싸서 요청 본문을 생성
        List<OrderDto> updateList = List.of(
                OrderDto.builder()
                        .orderId(nonExistentOrderId) // 존재하지 않는 ID
                        .customerEmail(customerEmail)
                        .quantity(5)
                        .build()
        );

        ResultActions resultActions = mvc
                .perform(
                        put("/orders/{orderId}", nonExistentOrderId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateList)) // List를 전송
                )
                .andDo(print());


        resultActions
                .andExpect(handler().handlerType(OrderController.class))
                .andExpect(handler().methodName("updateOrder"))
                .andExpect(status().isNotFound()) // 404
                .andExpect(jsonPath("$.resultCode").value("404-1"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 데이터입니다."));
    }
}
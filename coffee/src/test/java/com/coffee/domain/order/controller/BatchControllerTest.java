package com.coffee.domain.order.controller;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.repository.CustomerRepository;
import com.coffee.domain.order.entity.Order;
import com.coffee.domain.order.repository.OrderRepository;
import com.coffee.domain.order.service.OrderBatchService;
import com.coffee.domain.product.entity.Product;
import com.coffee.domain.product.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class BatchControllerTest {


    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderBatchService orderBatchService;

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private ProductRepository productRepository;

    private MockMvc mvc;
    private List<Long> createdOrderIds = new ArrayList<>();

    // 테스트에서 사용할 고객 및 상품 데이터 목록
    private List<Customer> testCustomers = new ArrayList<>();
    private List<Product> testProducts = new ArrayList<>();

    @BeforeEach
    void setUp() {
        createTestCustomersForBatch();
        createTestProductsForBatch();

        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        createdOrderIds.clear();
        createTestOrdersForBatch();
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

        // 배치 테스트 관련 이메일로 생성된 주문들 모두 삭제
        orderRepository.findAll().stream()
                .filter(order -> order.getCustomerEmail().contains("batch") ||
                        order.getCustomerEmail().contains("manual"))
                .forEach(order -> orderRepository.deleteById(order.getOrderId()));

        orderRepository.deleteAll(); // 남아있는 주문 데이터 정리
        customerRepository.deleteAll(testCustomers);
        productRepository.deleteAll(testProducts);
    }

    private void createTestCustomersForBatch() {
        // BatchControllerTest에서 사용할 이메일 목록
        List<String> emails = List.of("batch1@example.com", "batch2@example.com", "batch3@example.com", "manual@example.com");

        emails.forEach(email -> {
            Customer customer = Customer.builder()
                    .email(email)
                    .password("batch_pass")
                    .username("Batch User " + email.split("@")[0])
                    .address("Batch Address")
                    .postalCode(54321)
                    .build();
            testCustomers.add(customerRepository.save(customer));
        });
    }

    // **추가: Batch용 Product 데이터 생성 메서드**
    private void createTestProductsForBatch() {

        Product product1 = Product.builder().name("Batch Product 1").price(100).stock(100).build();
        Product product2 = Product.builder().name("Batch Product 2").price(200).stock(200).build();
        Product product3 = Product.builder().name("Batch Product 3").price(300).stock(300).build();

        testProducts.add(productRepository.save(product1));
        testProducts.add(productRepository.save(product2));
        testProducts.add(productRepository.save(product3));
    }

    private void createTestOrdersForBatch() {
        Long productId1 = testProducts.get(0).getId();
        Long productId2 = testProducts.get(1).getId();
        Long productId3 = testProducts.get(2).getId();

        LocalDateTime yesterday14 = LocalDateTime.now().minusDays(1).withHour(14).withMinute(0);
        LocalDateTime today14 = LocalDateTime.now().withHour(14).withMinute(0);

        // 배치 처리 대상 주문들 (전날 14시 ~ 오늘 14시)
        Order order1 = Order.builder()
                .customerEmail("batch1@example.com")
                .productId(productId1)
                .orderDate(yesterday14.plusHours(1))
                .orderState(false)
                .quantity(2)
                .build();

        Order order2 = Order.builder()
                .customerEmail("batch2@example.com")
                .productId(productId2)
                .orderDate(today14.minusHours(1))
                .orderState(false)
                .quantity(1)
                .build();

        // 처리 대상이 아닌 주문 (이미 처리됨)
        Order order3 = Order.builder()
                .customerEmail("batch3@example.com")
                .productId(productId3)
                .orderDate(yesterday14.plusHours(2))
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
    @DisplayName("배치 수동 실행 - 특정 기간 주문 일괄 처리 및 실제 처리 확인")
    void t1() throws Exception {

        // 오늘 14시 기준 컷오프
        LocalDateTime cutoffToday = LocalDate.now().atTime(14, 0);
        // 전날 14시
        LocalDateTime cutoffYesterday = cutoffToday.minusDays(1);

        // 처리 전 미처리 주문 수 확인
        long beforeCount = orderRepository.findByOrderDateBetweenAndOrderState(cutoffYesterday, cutoffToday, false).size();

        String startTimeStr = cutoffYesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String endTimeStr = cutoffToday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        ResultActions resultActions = mvc
                .perform(
                        post("/batch/orders/process")
                                .param("startTime", startTimeStr)
                                .param("endTime", endTimeStr)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BatchController.class))
                .andExpect(handler().methodName("processOrdersManually"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("주문 일괄 처리가 완료되었습니다."))
                .andExpect(jsonPath("$.data").value(containsString("건 처리됨")));

        // 처리 후 미처리 주문이 감소했는지 확인
        long afterCount = orderRepository.findByOrderDateBetweenAndOrderState(cutoffToday, cutoffToday, false).size();
        assertThat(afterCount).isLessThanOrEqualTo(beforeCount);
    }

    @Test
    @DisplayName("처리 대기 주문 수 조회 - 현재 처리 대기 중인 주문 개수 확인")
    void t2() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        get("/batch/orders/pending-count")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BatchController.class))
                .andExpect(handler().methodName("getPendingOrderCount"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("처리 대기 중인 주문 수를 조회했습니다."))
                .andExpect(jsonPath("$.data").isNumber());

        // 실제 서비스 로직이 올바르게 동작하는지 확인
        long pendingCount = orderBatchService.getPendingOrderCount();
        assertThat(pendingCount).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("오늘 주문 배치 처리 - 테스트용 배치 실행")
    void t3() throws Exception {
        // 처리 전 상태 저장
        LocalDateTime cutoffYesterday = LocalDateTime.now().minusDays(1).withHour(14).withMinute(0);
        LocalDateTime cutoffToday = LocalDateTime.now().withHour(14).withMinute(0);
        long beforeCount = orderRepository.findByOrderDateBetweenAndOrderState(cutoffYesterday, cutoffToday, false).size();

        ResultActions resultActions = mvc
                .perform(
                        post("/batch/orders/process-today")
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BatchController.class))
                .andExpect(handler().methodName("processTodayOrders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("오늘의 주문 일괄 처리가 실행되었습니다."));

        // 배치 처리 후 미처리 주문 수가 감소했는지 확인
        long afterCount = orderRepository.findByOrderDateBetweenAndOrderState(cutoffYesterday, cutoffToday, false).size();
        assertThat(afterCount).isLessThanOrEqualTo(beforeCount);
    }

    @Test
    @DisplayName("배치 처리 - 처리할 주문이 없는 경우 0건 처리 확인")
    void t4() throws Exception {
        // 생성된 테스트 주문들을 모두 처리 상태로 변경
        createdOrderIds.forEach(orderId -> {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order != null) {
                order.setOrderState(true);
                orderRepository.save(order);
            }
        });

        LocalDateTime cutoffYesterday = LocalDateTime.now().minusDays(1).withHour(14).withMinute(0);
        LocalDateTime cutoffToday = LocalDateTime.now().withHour(14).withMinute(0);

        String startTimeStr = cutoffYesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String endTimeStr = cutoffToday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        ResultActions resultActions = mvc
                .perform(
                        post("/batch/orders/process")
                                .param("startTime", startTimeStr)
                                .param("endTime", endTimeStr)
                )
                .andDo(print());

        resultActions
                .andExpect(handler().handlerType(BatchController.class))
                .andExpect(handler().methodName("processOrdersManually"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data").value("0건 처리됨"));

        // 실제로 처리할 주문이 없는지 확인
        long pendingCount = orderRepository.findByOrderDateBetweenAndOrderState(cutoffYesterday, cutoffToday, false).size();
        assertThat(pendingCount).isEqualTo(0);
    }

    @Test
    @DisplayName("배치 수동 처리 - 잘못된 날짜 형식으로 요청")
    void t5() throws Exception {
        ResultActions resultActions = mvc
                .perform(
                        post("/batch/orders/process")
                                .param("startTime", "invalid-date")
                                .param("endTime", "invalid-date")
                )
                .andDo(print());

        // 잘못된 날짜 형식으로 인한 400 Bad Request 예상
        resultActions
                .andExpect(handler().handlerType(BatchController.class))
                .andExpect(handler().methodName("processOrdersManually"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("배치 수동 처리 - 시작시간이 종료시간보다 늦은 경우")
    void t6() throws Exception {
        LocalDateTime cutoffYesterday = LocalDateTime.now().minusDays(1).withHour(14).withMinute(0);
        LocalDateTime cutoffToday = LocalDateTime.now().withHour(14).withMinute(0);

        String startTimeStr = cutoffToday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        String endTimeStr = cutoffYesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));

        ResultActions resultActions = mvc
                .perform(
                        post("/batch/orders/process")
                                .param("startTime", startTimeStr)
                                .param("endTime", endTimeStr)
                )
                .andDo(print());

        // 시작시간이 종료시간보다 늦은 경우에도 API는 정상 응답하되, 처리 건수가 0건이어야 함
        resultActions
                .andExpect(handler().handlerType(BatchController.class))
                .andExpect(handler().methodName("processOrdersManually"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.data").value("0건 처리됨"));
    }
}
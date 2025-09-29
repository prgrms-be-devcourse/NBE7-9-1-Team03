package com.coffee.domain.customer.controller;


import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.repository.CustomerRepository;
import com.coffee.domain.customer.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;

    String email = "test@example.com";
    String password = "password123";

    @BeforeEach
    void setUp() {
        customerRepository.deleteAll();
    }

    // 🔹 1) 스케줄러 Purge 테스트
    @Test
    void purgeDeletedCustomers_shouldDeleteOldMarkedCustomers() {
        // given
        Customer customer1 = new Customer(email, "encoded", "name", "address", 12345);
        customer1.markDeleted();
        // 과거 날짜로 세팅
        customer1.markDeleted();
        customerRepository.save(customer1);

        // when
        customerService.purgeDeletedCustomers();

        // then
        List<Customer> all = customerRepository.findAll();
        assertThat(all).isEmpty();
    }

    // 🔹 2) 회원가입 -> 로그인 -> 내 정보 조회
    @Test
    void join_then_login_then_getMe() throws Exception {
        // 회원가입
        mockMvc.perform(post("/customer/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "username": "테스터",
                                  "address": "서울시 강남구",
                                  "postalCode": "12345"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201"));

        // 로그인
        mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"));

        // 내 정보 조회
        mockMvc.perform(get("/customer/me")
                        .cookie()) // 쿠키 포함해야 실제 액세스됨
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.customerDto.email").value(email));
    }

    // 🔹 3) 회원정보 수정
    @Test
    void modifyMe_shouldUpdateCustomerInfo() throws Exception {
        // given
        customerService.join(email, password, "테스터", "서울시 강남구", 12345);
        Customer saved = customerRepository.findByEmail(email).get();

        // 로그인 후 쿠키세팅
        customerService.login(email, password);

        mockMvc.perform(put("/customer/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "username": "수정된이름",
                                  "address": "수정된주소",
                                  "postalCode": "54321"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));

        Customer updated = customerRepository.findByEmail(email).get();
        assertThat(updated.getUsername()).isEqualTo("수정된이름");
    }

    // 🔹 4) 회원탈퇴 (soft delete)
    @Test
    void quit_shouldMarkCustomerAsDeleted() throws Exception {
        // given
        customerService.join(email, password, "테스터", "서울시 강남구", 12345);
        customerService.login(email, password);

        mockMvc.perform(delete("/customer/quit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"));

        Customer quitCustomer = customerRepository.findByEmail(email).get();
        assertThat(quitCustomer.isDeleted()).isTrue();
    }
}
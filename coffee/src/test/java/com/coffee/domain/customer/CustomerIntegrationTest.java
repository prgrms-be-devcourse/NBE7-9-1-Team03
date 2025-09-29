package com.coffee.domain.customer;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.repository.CustomerRepository;
import com.coffee.domain.customer.service.CustomerService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CustomerController + CustomerService 통합테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    CustomerService customerService;

    String email = "test@example.com";
    String password = "password123";

    @BeforeEach
    void setUp() throws Exception {
        if (customerRepository.findByEmail(email).isEmpty()) {
            mockMvc.perform(post("/customer/join")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "email": "%s",
                                  "password": "%s",
                                  "username": "기본회원",
                                  "address": "서울시 강남구",
                                  "postalCode": "12345"
                                }
                                """.formatted(email, password)))
                    .andExpect(status().isOk());
        }
    }


    @Test
    @DisplayName("탈퇴 사용자 스케줄러 Purge 테스트")
    void t1() {
        // given
        Customer deletedUser = new Customer(email, "encoded-password", "테스터", "서울시 강남구", 12345);
        deletedUser.markDeleted();
        deletedUser.setDeletedAt(LocalDateTime.now().minusDays(40)); // 30일 이상 경과
        customerRepository.save(deletedUser);

        // when
        customerService.purgeDeletedCustomers();

        // then
        Optional<Customer> result = customerRepository.findByEmail(email);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("회원가입, 로그인, 내정보 조회 성공")
    void t2() throws Exception {
        // 매 테스트마다 고유한 이메일 생성
        String uniqueEmail = "testuser_" + System.nanoTime() + "@example.com";

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
                    """.formatted(uniqueEmail, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("201"));

        // 로그인
        MvcResult loginResult = mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(uniqueEmail, password)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andExpect(cookie().exists("refreshToken"))
                .andReturn();

        Cookie accessToken = loginResult.getResponse().getCookie("accessToken");
        Cookie refreshToken = loginResult.getResponse().getCookie("refreshToken");

        // 내 정보 조회
        mockMvc.perform(get("/customer/me")
                        .cookie(accessToken, refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.customerDto.email").value(uniqueEmail));
    }

    @Test
    @DisplayName("탈퇴한 사용자 로그인 실패 케이스 테스트")
    void t3() throws Exception {
        String uniqueEmail = "testuser_" + System.nanoTime() + "@example.com";

        // 회원가입
        mockMvc.perform(post("/customer/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "%s",
                      "password": "%s",
                      "username": "테스터",
                      "address": "서울시 강남구",
                      "postalCode": 12345
                    }
                    """.formatted(uniqueEmail, password)))
                .andExpect(status().isOk());

        // 로그인 → accessToken 추출
        MvcResult loginResult = mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(uniqueEmail, password)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("accessToken"))
                .andReturn();

        Cookie accessToken = loginResult.getResponse().getCookie("accessToken");

        // 탈퇴 요청 (이메일, 비밀번호 포함한 JSON 바디 전송)
        mockMvc.perform(delete("/customer/quit")
                        .cookie(accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(uniqueEmail, password)))
                .andExpect(status().isOk());

        // 다시 로그인 시도 → 실패
        mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "email": "%s",
                      "password": "%s"
                    }
                    """.formatted(uniqueEmail, password)))
                .andExpect(jsonPath("$.resultCode").value("401"))
                .andExpect(jsonPath("$.msg").value("탈퇴한 계정입니다."));
    }
    @Test
    @DisplayName("로그인 시 이메일 또는 비밀번호 불일치 케이스 테스트")
    void t4() throws Exception {

        // 이메일 불일치
        mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "wrong@example.com",
                              "password": "%s"
                            }
                            """.formatted(password)))
                .andExpect(jsonPath("$.resultCode").value("401"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 아이디 입니다."));

        // 비밀번호 불일치
        mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "email": "%s",
                              "password": "wrong-password"
                            }
                            """.formatted(email)))
                .andExpect(jsonPath("$.resultCode").value("401"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다"));
    }

    @Test
    @DisplayName("내 정보 수정 시 비밀번호 불일치 케이스 테스트")
    void t5() throws Exception {
        // 로그인 및 accessToken 쿠키 저장
        MvcResult result = mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");

        mockMvc.perform(put("/customer/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(accessToken)
                        .content("""
                        {
                          "email": "%s",
                          "password": "wrong-password",
                          "username": "수정된이름",
                          "address": "수정된 주소",
                          "postalCode": "54321"
                        }
                        """.formatted(email)))
                .andExpect(jsonPath("$.resultCode").value("401"))
                .andExpect(jsonPath("$.msg").value("비밀번호가 일치하지 않습니다"));
    }

    @Test
    @DisplayName("내 정보 수정 정상 동작 테스트")
    void t6() throws Exception {
        // 로그인 및 accessToken 쿠키 저장
        MvcResult result = mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessToken = result.getResponse().getCookie("accessToken");

        mockMvc.perform(put("/customer/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(accessToken)
                        .content("""
                        {
                          "email": "%s",
                          "password": "%s",
                          "username": "수정된이름",
                          "address": "수정된 주소",
                          "postalCode": "54321"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("내 정보 수정 성공"));

        // 실제 DB 값도 검증해보자
        Customer updatedCustomer = customerRepository.findByEmail(email).orElseThrow();
        assertThat(updatedCustomer.getUsername()).isEqualTo("수정된이름");
        assertThat(updatedCustomer.getAddress()).isEqualTo("수정된 주소");
        assertThat(updatedCustomer.getPostalCode()).isEqualTo(54321);
    }

    @Test
    @DisplayName("로그아웃 시 쿠키 삭제 테스트")
    void t7() throws Exception {

        MvcResult result1 = mockMvc.perform(post("/customer/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie accessToken = result1.getResponse().getCookie("accessToken");

        MvcResult result2 = mockMvc.perform(delete("/customer/logout")
                        .cookie(accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("로그아웃 되었습니다."))
                .andReturn();

        Cookie[] responseCookies = result2.getResponse().getCookies();

        // 쿠키가 두 개 이상 설정되어야 함
        assertThat(responseCookies.length).isGreaterThanOrEqualTo(2);

        // accessToken과 refreshToken이 만료되었는지 확인
        boolean accessTokenDeleted = Arrays.stream(responseCookies)
                .anyMatch(cookie -> cookie.getName().equals("accessToken") && cookie.getMaxAge() == 0);
        boolean refreshTokenDeleted = Arrays.stream(responseCookies)
                .anyMatch(cookie -> cookie.getName().equals("refreshToken") && cookie.getMaxAge() == 0);

        assertThat(accessTokenDeleted).isTrue();
        assertThat(refreshTokenDeleted).isTrue();
    }
}
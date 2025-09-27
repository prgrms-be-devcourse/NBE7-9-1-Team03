package com.coffee.global.rq;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.service.AuthService;
import com.coffee.domain.customer.service.CustomerService;
import com.coffee.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final CustomerService customerService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;
    private final AuthService authService;

    /*
    Http요청에 담긴 헤더, 쿠키정보를 보고 현재 로그인 중인 사용자를 가져오는 메서드입니다.
    Rq.getActor() 와 CustomerService.findByEmail
     */
    public Customer getActor() {
        String accessToken;
        String refreshToken;

        String headerAuthorization = getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer "))
                throw new ServiceException("401-1", "Authorization 헤더가 Bearer 형식이 아닙니다.");

            String[] bits = headerAuthorization.split(" ");
            accessToken = bits.length >= 2 ? bits[1] : "";
            refreshToken = bits.length >= 3 ? bits[2] : "";
        } else {
            accessToken = getCookieValue("accessToken", "");
            refreshToken = getCookieValue("refreshToken", "");
        }

        if(accessToken.isBlank() && refreshToken.isBlank()){
            throw new ServiceException("401-2", "로그인 후 이용해주세요.");
        }

        Customer customer = null;
        boolean isAccessTokenValid = false;

        // 액세쓰 토큰 검증
        if(!accessToken.isBlank() && authService.validateToken(accessToken)){
            Map<String, Object> payload = authService.parseToken(accessToken);
            String email = (String) payload.get("email");
            customer = customerService.findByEmail(email)
                    .orElseThrow(() -> new ServiceException("401-3", "존재하지 않는 사용자"));
            isAccessTokenValid = true;
        }
        // 리프래시 검증, 액세스가 만료상태면 재발급 / 리프래시가 안맞으면 예외던지기
        if (!isAccessTokenValid && !refreshToken.isBlank()) {
            Map<String, Object> payload = authService.parseToken(refreshToken);
            if(payload == null){
                throw new ServiceException("401-4", "유효하지 않은 RefreshToken입니다.");
            }
            String email = (String) payload.get("email");
            customer = customerService.findByEmail(email)
                    .orElseThrow(() -> new ServiceException("401-5", "존재하지 않는 사용자"));

            // 새로운 AccessToken 발급
            String newAccessToken = authService.genAccessToken(customer);

            // 쿠키 및 헤더에 갱신
            setCookie("accessToken", newAccessToken);
            setHeader("accessToken", newAccessToken);
        }

        if (customer == null) {
            throw new ServiceException("401-6", "인증 실패");
        }

        return customer;
    }

    // 관리자 권한 검증
    public void validateAdminActor() {
        Customer actor = getActor();
        if (actor.getRole() != 1) {
            throw new ServiceException("401-7", "관리자 권한이 필요합니다.");
        }
    }

    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    public String getHeader(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getHeader(name))
                .filter(headerValue -> !headerValue.isBlank())
                .orElse(defaultValue);
    }

    public String getCookieValue(String name, String defaultValue) {
        return Optional
                .ofNullable(request.getCookies())
                .flatMap(
                        cookies ->
                                Arrays.stream(cookies)
                                        .filter(cookie -> cookie.getName().equals(name))
                                        .map(Cookie::getValue)
                                        .filter(value -> !value.isBlank())
                                        .findFirst()
                )
                .orElse(defaultValue);
    }

    public void setCookie(String name, String value) {
        if (value == null) value = "";

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setDomain("localhost");

        // 값이 없다면 해당 쿠키변수를 삭제하라는 뜻
        if (value.isBlank()) {
            cookie.setMaxAge(0);
        }

        response.addCookie(cookie);
    }

    public void deleteCookie(String name) {
        setCookie(name, null);
    }
}

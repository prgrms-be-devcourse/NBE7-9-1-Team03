package com.coffee.global.rq;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.service.CustomerService;
import com.coffee.global.exception.ServiceException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Rq {
    private final CustomerService customerService;
    private final HttpServletRequest request;
    private final HttpServletResponse response;

    public Customer getActor() {
        String apiKey;
        String headerAuthorization = getHeader("Authorization", "");

        if (!headerAuthorization.isBlank()) {
            if (!headerAuthorization.startsWith("Bearer "))
                throw new ServiceException("401", "Authorization 헤더가 Bearer 형식이 아닙니다.");

            String[] headerAuthorizationBits = headerAuthorization.split(" ", 3);
            apiKey = headerAuthorizationBits[1];
        } else {
            apiKey = getCookieValue("apiKey", "");
        }

        if (apiKey.isBlank())
            throw new ServiceException("401", "로그인 후 이용해주세요.");

        Customer actor = customerService.findByApiKey(apiKey)
                .orElseThrow(() -> new ServiceException("401", "API 키가 올바르지 않습니다."));

        return actor;
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

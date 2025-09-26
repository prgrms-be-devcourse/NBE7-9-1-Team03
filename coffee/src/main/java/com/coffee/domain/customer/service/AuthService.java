package com.coffee.domain.customer.service;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.global.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;

    public String genAccessToken(Customer customer) {
        return jwtProvider.generateAccessToken(customer);
    }

    public String genRefreshToken(Customer customer) {
        return jwtProvider.generateRefreshToken(customer);
    }

    public boolean validateToken(String token) {
        return jwtProvider.isValid(token);
    }

    public Map<String, Object> parseToken(String token) {
        return jwtProvider.payloadOrNull(token);
    }
}

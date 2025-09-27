package com.coffee.global.jwt;

import com.coffee.domain.customer.entity.Customer;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ClaimsBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtProvider {

    private final SecretKey secretKey = Keys.hmacShaKeyFor("my-very-secure-secret-key-which-is-very-long".getBytes(StandardCharsets.UTF_8));
    private final long accessTokenValidity = 1000L * 60 * 15;           // 15분
    private final long refreshTokenValidity = 1000L * 60 * 60 * 24 * 7; // 7일

    public String generateAccessToken(Customer customer) {
        Claims claims = createClaims(customer);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(Customer customer) {
        Claims claims = createClaims(customer);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(secretKey)
                .compact();
    }

    public Claims payloadOrNull(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public Claims createClaims(Customer customer) {

        Map<String, Object> body = Map.of(
                "email", customer.getEmail(),
                "username", customer.getUsername(),
                "address", customer.getAddress(),
                "role", customer.getRole()
        );

        ClaimsBuilder claimsBuilder = Jwts.claims();

        for (Map.Entry<String, Object> entry : body.entrySet()) {
            claimsBuilder.add(entry.getKey(), entry.getValue());
        }

        return claimsBuilder.build();
    }

    public boolean isValid(String token) {
        return payloadOrNull(token) != null;
    }
}

package com.coffee.domain.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Customer{
    @Id
    @Column(unique=true)
    private String email;       // 사용자 이메일(아이디로 사용)

    @Column(nullable = false)
    private String password;    // 비밀번호

    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    private String username;    // 이름
    private String address;     // 주소
    private Integer postalCode;  // 우편번호

    @Column(nullable = false)
    private int role = 0; // 0 = USER, 1 = ADMIN

    @Column(nullable = false)
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    public Customer(String email, String password, String username, String address, Integer postalCode) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.address = address;
        this.postalCode = postalCode;
    }

    public void updateInfo(String username, String address, Integer postalCode) {
        this.username = username;
        this.address = address;
        this.postalCode = postalCode;
    }

    public void updateAddress(String address, Integer postalCode) {
        this.address = address;
        this.postalCode = postalCode;
    }
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void clearRefreshToken() {
        this.refreshToken = null;
    }

    public void markDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}

/*
관리자 권한 업데이트 > 일반유저와 동일하게 회원가입, 권한만 업데이트
UPDATE customer SET role = 1 WHERE email = 'admin@coffee.com';
 */

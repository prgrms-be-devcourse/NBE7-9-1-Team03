package com.coffee.domain.customer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Customer{
    @Id
    @Column(unique=true)
    private String email;       // 사용자 이메일(아이디로 사용)

    @Column( nullable = false)
    private String password;    // 비밀번호

    @Column(name = "refresh_token")
    private String refreshToken;

    private String username;    // 이름
    private String address;     // 주소
    private Integer postalCode;  // 우편번호

    @Column(nullable = false)
    private int role = 0; // 0 = USER, 1 = ADMIN

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

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public void clearRefreshToken() {
        this.refreshToken = null;
    }
}

/*
User 유저
회원정보 id PK INT
이메일 email UNIQUE KEY varchar
유저명 username notnull varchar
주소 addr notnull varchar
우편번호 postalCode notnull varchar
어드민 isadmin notnull boolean -> 추후 구현
 */

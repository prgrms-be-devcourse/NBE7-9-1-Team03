package com.coffee.domain.customer.entity;

import com.coffee.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Customer extends BaseEntity {

    @Column(unique=true)
    private String email;       // 사용자 이메일(아이디로 사용)

    private String password;    // 비밀번호

    @Column(unique=true)
    private String apiKey;

    private String username;    // 이름
    private String address;     // 주소
    private Integer postalCode;  // 우편번호

    public Customer(String email, String password, String username, String address, Integer postalCode) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.address = address;
        this.postalCode = postalCode;
        this.apiKey = UUID.randomUUID().toString();     // JWT or 세션/시큐리티 미도입으로 UUID사용
    }

    public void updateInfo(String username, String address, Integer postalCode) {
        this.username = username;
        this.address = address;
        this.postalCode = postalCode;
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

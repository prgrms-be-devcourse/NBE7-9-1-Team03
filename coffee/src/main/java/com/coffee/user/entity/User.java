package com.coffee.user.entity;

import com.coffee.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
public class User extends BaseEntity {

    @Column(unique=true)
    private String email;       // 사용자 이메일

    private String username;    // 사용자 실명
    private String address;     // 사용자 주소
    private Integer postalCode;  // 사용자 우편번호
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

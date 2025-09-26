package com.coffee.domain.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerCommonReqBody(
        @NotBlank
        @Pattern(
                regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                message = "올바른 이메일 형식이 아닙니다."
        )
        String email,

        @NotBlank
        @Size(min = 8, max = 30)
        String password,

        @NotBlank
        @Size(min = 2, max = 30)
        String username,

        @NotBlank
        @Pattern(
                regexp = "^[가-힣a-zA-Z0-9\\s\\-]{5,100}$",
                message = "주소는 한글, 영문, 숫자, 공백, 하이픈(-)만 사용 가능하며 5~100자 이내여야 합니다."
        )
        String address,

        @NotBlank
        @Pattern(regexp = "\\d{5}", message = "우편번호는 5자리 숫자여야 합니다.")
        String postalCode
) {}
package com.coffee.domain.user.controller;

import com.coffee.domain.user.dto.CustomerDto;
import com.coffee.domain.user.entity.Customer;
import com.coffee.domain.user.service.CustomerService;
import com.coffee.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/customer")
@Tag(name = "ApiV1CustomerController", description = "고객정보 API")
public class ApiV1CustomerController {

    private final CustomerService customerService;

    @GetMapping("/email/{email}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-이메일")
    public CustomerDto getUserByEmail(@PathVariable String email) {
        Customer customer = customerService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new CustomerDto(customer);
    }

    @GetMapping("/id/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-id")
    public CustomerDto getUserById(@PathVariable Long id) {
        Customer customer = customerService.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new CustomerDto(customer);
    }

    record JoinReqBody(
            @NotBlank
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                    message = "올바른 이메일 형식이 아닙니다."
            )
            String email,
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
    record JoinResBody(
            CustomerDto customerDto
    ) {}
    @PostMapping("/join")
    @Operation(summary = "고객정보 저장")
    public RsData<CustomerDto> join(
            @RequestBody @Valid JoinReqBody reqBody
    ){

        Customer customer = customerService
                .join(reqBody.email, reqBody.username, reqBody.address, Integer.parseInt(reqBody.postalCode()));

        return new RsData(
                "201",
                "고객정보가 저장되었습니다.",
                new JoinResBody(
                        new CustomerDto(customer)
                )
        );
    }
}

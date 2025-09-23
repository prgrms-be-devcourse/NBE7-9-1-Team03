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

    @GetMapping("/{email}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-이메일")
    public CustomerDto getUserByEmail(@PathVariable String email) {
        Customer customer = customerService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new CustomerDto(customer);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-id")
    public CustomerDto getUserById(@PathVariable Long id) {
        Customer customer = customerService.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new CustomerDto(customer);
    }

    record JoinReqBody(
            @NotBlank
            @Size(min = 2, max = 30)
            String email,
            @NotBlank
            @Size(min = 2, max = 30)
            String username,
            @NotBlank
            @Size(min = 2, max = 30)
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

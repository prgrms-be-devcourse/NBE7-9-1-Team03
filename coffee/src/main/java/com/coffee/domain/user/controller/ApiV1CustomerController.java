package com.coffee.domain.user.controller;

import com.coffee.domain.user.dto.CustomerCommonResBody;
import com.coffee.domain.user.dto.CustomerDto;
import com.coffee.domain.user.dto.CustomerCommonReqBody;
import com.coffee.domain.user.entity.Customer;
import com.coffee.domain.user.service.CustomerService;
import com.coffee.global.exception.ServiceException;
import com.coffee.global.rq.Rq;
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
    private final Rq rq;


    // 이메일로 바꾸기
    @GetMapping("/email/{email}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-email")
    public CustomerDto getUserById(@PathVariable String email) {
        Customer customer = customerService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new CustomerDto(customer);
    }

    @PostMapping("/join")
    @Operation(summary = "회원가입")
    public RsData<CustomerDto> join(
            @RequestBody @Valid CustomerCommonReqBody reqBody
    ) {

        Customer customer = customerService
                .join(reqBody.email(), reqBody.password(), reqBody.username(),
                        reqBody.address(), Integer.parseInt(reqBody.postalCode()));

        return new RsData(
                "201",
                "고객정보가 저장되었습니다.",
                new CustomerCommonResBody(
                        new CustomerDto(customer)
                )
        );
    }

    record LoginReqBody(
            @NotBlank
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                    message = "올바른 이메일 형식이 아닙니다."
            )
            String email,

            @NotBlank
            @Size(min = 2, max = 30)
            String password
    ) {
    }
    record LoginResBody(
            CustomerDto customerDto,
            String apiKey
    ) {
    }
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<CustomerDto> login(
            @RequestBody @Valid LoginReqBody reqBody
    ) {
        Customer customer = customerService.findByEmail(reqBody.email).orElseThrow(
                () -> new ServiceException("401", "존재하지 않는 아이디 입니다.")
        );

        customerService.checkPassword(reqBody.password, customer.getPassword());
        rq.setCookie("apiKey", customer.getApiKey());

        return new RsData(
                "200",
                "로그인 성공",
                new LoginResBody(
                        new CustomerDto(customer),
                        customer.getApiKey()
                )
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    public RsData<Void> logout() {

        rq.deleteCookie("apiKey");

        return new RsData<>(
                "200",
                "로그아웃 되었습니다."
        );
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내 정보 조회")
    public RsData<CustomerDto> getUserByEmail(@PathVariable String email) {
        Customer customer = customerService.findByEmail(rq.getActor().getEmail()).get();

        return new RsData(
                "200",
                "내 정보 조회 성공",
                new CustomerCommonResBody(
                        new CustomerDto(customer)
                )
        );
    }


    @PutMapping("/me")
    @Operation(summary = "회원 개인정보 변경")
    public RsData<Void> modifyMe(
            @RequestBody @Valid CustomerCommonReqBody reqBody
    ) {
        Customer actor = customerService.findByEmail(rq.getActor().getEmail()).get();

        // 로그인 중인 이메일 일치 체크
        if(!actor.getEmail().equals(rq.getActor().getEmail())) {
            throw new ServiceException("401", "로그인한 이메일과 다릅니다");
        }

        // 로그인 중인 비밀번호와 일치 체크
        customerService.checkPassword(actor.getPassword(), reqBody.password());

        customerService.modifyMe(actor, reqBody.username(), reqBody.address(), Integer.parseInt(reqBody.postalCode()));

        return new RsData(
                "200",
                "내 정보 수정 성공"
        );
    }
}

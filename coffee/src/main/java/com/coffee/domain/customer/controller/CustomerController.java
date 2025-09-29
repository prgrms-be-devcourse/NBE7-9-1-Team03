package com.coffee.domain.customer.controller;

import com.coffee.domain.customer.dto.CustomerCommonResBody;
import com.coffee.domain.customer.dto.CustomerDto;
import com.coffee.domain.customer.dto.CustomerCommonReqBody;
import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.service.AuthService;
import com.coffee.domain.customer.service.CustomerService;
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
@RequestMapping("/customer")
@Tag(name = "ApiV1CustomerController", description = "고객정보 API")
public class CustomerController {

    private final CustomerService customerService;
    private final Rq rq;
    private final AuthService authService;


    // 이메일로 바꾸기
    @GetMapping("/email/{email}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-email")
    public CustomerDto getUserById(@PathVariable String email) {
        Customer customer = customerService.findByEmail(email)
                .orElseThrow(() -> new ServiceException("401", "사용자를 찾을 수 없습니다."));
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
            String accessToken,
            String refreshToken
    ) {
    }
    @PostMapping("/login")
    @Operation(summary = "로그인")
    public RsData<CustomerDto> login(
            @RequestBody @Valid LoginReqBody reqBody
    ) {
        Customer customer = customerService.login(reqBody.email(), reqBody.password());

        String accessToken =  authService.genAccessToken(customer);
        String refreshToken = customer.getRefreshToken();

        rq.setCookie("accessToken", accessToken);
        rq.setCookie("refreshToken", refreshToken);

        return new RsData(
                "200",
                "로그인 성공",
                new LoginResBody(
                        new CustomerDto(customer),
                        accessToken,
                        refreshToken
                )
        );
    }

    @DeleteMapping("/logout")
    @Operation(summary = "로그아웃")
    public RsData<Void> logout() {
        Customer actor = customerService.findByEmail(rq.getActor().getEmail())
                .orElseThrow(() -> new ServiceException("401", "존재하지 않는 계정"));

        customerService.logout(actor);

        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return new RsData<>(
                "200",
                "로그아웃 되었습니다."
        );
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "내 정보 조회")
    public RsData<CustomerDto> getUserByEmail() {
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
        if (!actor.getEmail().equals(reqBody.email())) {
            throw new ServiceException("401", "로그인한 이메일과 다릅니다");
        }

        // 로그인 중인 비밀번호와 일치 체크
        customerService.checkPassword(reqBody.password(), actor.getPassword());

        customerService.modifyMe(actor, reqBody.username(), reqBody.address(), Integer.parseInt(reqBody.postalCode()));
        String accessToken =  authService.genAccessToken(actor);
        rq.setCookie("accessToken", accessToken);


        return new RsData(
                "200",
                "내 정보 수정 성공"
        );
    }


    /*
        탈퇴:
        로그인 상태에서 이메일 비밀번호 한번 더 입력받습니다
        로그인 중인 이메일 비밀번호와 일치여부 확인하고 탈퇴시켜줍니다.
     */
    record QuitReqBody(
            @NotBlank
            @Pattern(
                    regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
                    message = "올바른 이메일 형식이 아닙니다."
            )
            String email,

            @NotBlank
            @Size(min = 8, max = 30)
            String password
    ) {
    }
    @DeleteMapping("/quit")
    @Operation(summary = "회원 탈퇴")
    public RsData<Void> quit(
            @RequestBody @Valid QuitReqBody reqBody
    ) {
        Customer actor = customerService.findByEmail(rq.getActor().getEmail()).get();

        // 로그인 중인 이메일 일치 체크
        if (!actor.getEmail().equals(reqBody.email())) {
            throw new ServiceException("401", "로그인한 이메일과 다릅니다");
        }

        // 로그인 중인 비밀번호와 일치 체크
        customerService.checkPassword(reqBody.password(), actor.getPassword());

        customerService.quit(actor);

        rq.deleteCookie("accessToken");
        rq.deleteCookie("refreshToken");

        return new RsData(
                "200",
                "회원 탈퇴 성공"
        );
    }
}

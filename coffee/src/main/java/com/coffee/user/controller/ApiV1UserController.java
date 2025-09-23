package com.coffee.user.controller;

import com.coffee.global.rsData.RsData;
import com.coffee.user.dto.UserDto;
import com.coffee.user.entity.User;
import com.coffee.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
@Tag(name = "ApiV1UserController", description = "유저 API")
public class ApiV1UserController {

    private final UserService userService;

    @GetMapping("/{email}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-이메일")
    public UserDto getUserByEmail(@PathVariable String email) {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserDto(user);
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "고객 정보 조회-id")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        return new UserDto(user);
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
            Long postalCode
    ) {}
    record JoinResBody(
            UserDto userDto
    ) {}
    @PostMapping
    @Operation(summary = "유저 정보 저장")
    public RsData<UserDto> join(
            @RequestBody @Valid JoinReqBody reqBody
    ){
        User user = userService.join(reqBody.email, reqBody.username, reqBody.address, reqBody.postalCode);

        return new RsData(
                "201",
                "유저 정보가 저장되었습니다.",
                new JoinResBody(
                        new UserDto(user)
                )
        );
    }
}

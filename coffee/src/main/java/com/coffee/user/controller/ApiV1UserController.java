package com.coffee.user.controller;

import com.coffee.user.dto.UserDto;
import com.coffee.user.entity.User;
import com.coffee.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}

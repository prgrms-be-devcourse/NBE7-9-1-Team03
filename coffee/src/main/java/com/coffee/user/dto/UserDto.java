package com.coffee.user.dto;

import com.coffee.user.entity.User;

public record UserDto(
        Long id,
        String email,
        String username,
        String address,
        Long postalCode
) {
    public UserDto(User user){
        this(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getAddress(),
                user.getPostalCode()
        );
    }
}

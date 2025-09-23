package com.coffee.domain.user.dto;

import com.coffee.domain.user.entity.Customer;

public record CustomerDto(
        Long id,
        String email,
        String username,
        String address,
        Integer postalCode
) {
    public CustomerDto(Customer customer){
        this(
                customer.getId(),
                customer.getEmail(),
                customer.getUsername(),
                customer.getAddress(),
                customer.getPostalCode()
        );
    }
}

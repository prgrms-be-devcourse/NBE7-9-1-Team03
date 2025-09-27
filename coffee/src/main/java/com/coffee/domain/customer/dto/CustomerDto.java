package com.coffee.domain.customer.dto;

import com.coffee.domain.customer.entity.Customer;
import lombok.Builder;


@Builder
public record CustomerDto(
        String email,
        String username,
        String address,
        Integer postalCode
) {
    public CustomerDto(Customer customer){
        this(
                customer.getEmail(),
                customer.getUsername(),
                customer.getAddress(),
                customer.getPostalCode()
        );
    }

    public static CustomerDto from(Customer customer) {
        CustomerDtoBuilder builder = CustomerDto.builder()
                .email(customer.getEmail())
                .username(customer.getUsername())
                .address(customer.getAddress())
                .postalCode(customer.getPostalCode());

        return builder.build();
    }

    public Customer toEntity() {
        return Customer.builder()
                .email(this.email)
                .username(this.username)
                .address(this.address)
                .postalCode(this.postalCode)
                .build();
    }
}

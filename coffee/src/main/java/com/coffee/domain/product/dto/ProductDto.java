package com.coffee.domain.product.dto;

import com.coffee.domain.customer.dto.CustomerDto;
import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.product.entity.Product;
import lombok.Builder;

@Builder
public record ProductDto(
        Long id,
        String name,
        int price,
        int stock
) {
    public ProductDto(Product p) {
        this(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }

    public static ProductDto from(Product product) {
        ProductDtoBuilder builder = ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock());

        return builder.build();
    }

    public Product toEntity() {
        return Product.builder()
                .id(this.id)
                .name(this.name)
                .price(this.price)
                .stock(this.stock)
                .build();
    }
}
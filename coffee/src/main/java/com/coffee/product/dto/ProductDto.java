package com.coffee.product.dto;

import com.coffee.product.entity.Product;


public record ProductDto(
        Long id,
        String name,
        int price,
        int stock
) {
    public ProductDto(Product p) {
        this(p.getId(), p.getName(), p.getPrice(), p.getStock());
    }
}
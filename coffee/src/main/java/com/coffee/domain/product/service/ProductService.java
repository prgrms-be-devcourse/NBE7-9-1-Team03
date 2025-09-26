package com.coffee.domain.product.service;


import com.coffee.domain.product.entity.Product;
import com.coffee.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);

    }

    @Transactional
    public Product create(String name, int price, int stock) {
        Product p = new Product(name, price, stock);
        return productRepository.save(p);
    }

    @Transactional
    public void modify(Product p, String name, int price, int stock) {
        p.modify(name, price, stock);
    }

    @Transactional
    public void delete(Product p) {
        productRepository.delete(p);
    }

}
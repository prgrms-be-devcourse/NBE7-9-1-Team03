package com.coffee.domain.order.product.repository;

import com.rest1.domain.post.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductRepository extends JpaRepository<Post, Long> {
}
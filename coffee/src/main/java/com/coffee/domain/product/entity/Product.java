package com.coffee.domain.product.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = true, length = 255)
    private String imageUrl;


    public Product(String name, int price, int stock, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    public void modify(String name, int price, int stock, String imageUrl) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
    }

    /**
     * 재고를 감소시키는 비즈니스 로직 메서드 (추가자 김경민)
     * @param quantity 주문 수량
     */
    public void decreaseStock(int quantity) {
        if (this.stock < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.stock -= quantity;
    }

    /**
     * 재고를 증가시키는 비즈니스 로직 메서드 (주문 취소, 반품 시 사용 추가자 김경민)
     * @param quantity 증가시킬 수량
     */
    public void increaseStock(int quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("재고 증가 수량은 0보다 작을 수 없습니다.");
        }
        this.stock += quantity;
    }

}

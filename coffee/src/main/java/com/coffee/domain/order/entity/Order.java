package com.coffee.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "order_state", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean orderState = false;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Customer와의 다대일 관계 - customer_email 컬럼을 매핑합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_email", referencedColumnName = "email", insertable = false, updatable = false)
    private Customer customer;

    // Product와의 다대일 관계 - product_id 컬럼을 매핑합니다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
}
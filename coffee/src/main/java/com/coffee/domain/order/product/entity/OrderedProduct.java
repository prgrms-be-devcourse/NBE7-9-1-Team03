package com.coffee.domain.order.product.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import com.coffee.domain.order.entity.Order;
import com.coffee.domain.product.entity.Product;

@Entity
@Table(name = "ordered_product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderedProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ordered_product_id")
    private Integer orderedProductId;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "product_id", nullable = false)
    private Integer productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    // Order와의 다대일 관계 - 주문이 삭제되면 주문상품도 삭제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", insertable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Order order;

    // Product와의 다대일 관계 - 상품 삭제와 무관하게 유지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private Product product;
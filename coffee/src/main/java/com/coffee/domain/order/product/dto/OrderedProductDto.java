package com.coffee.domain.order.product.dto;

import com.coffee.domain.order.product.entity.OrderedProduct;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderedProductDto {

    private Integer orderedProductId;
    private Integer orderId;
    private Integer productId;
    private Integer quantity;

    // 필요시 연관 엔티티 정보
    private String productName;
    private Integer productPrice;

    // Entity to DTO
    public static OrderedProductDto from(OrderedProduct orderedProduct) {
        OrderedProductDtoBuilder builder = OrderedProductDto.builder()
                .orderedProductId(orderedProduct.getOrderedProductId())
                .orderId(orderedProduct.getOrderId())
                .productId(orderedProduct.getProductId())
                .quantity(orderedProduct.getQuantity());

        // Product 정보가 있다면 추가
        if (orderedProduct.getProduct() != null) {
            builder.productName(orderedProduct.getProduct().getProductName())
                    .productPrice(orderedProduct.getProduct().getPrice());
        }

        return builder.build();
    }

    // DTO to Entity
    public OrderedProduct toEntity() {
        return OrderedProduct.builder()
                .orderedProductId(this.orderedProductId)
                .orderId(this.orderId)
                .productId(this.productId)
                .quantity(this.quantity)
                .build();
    }
}
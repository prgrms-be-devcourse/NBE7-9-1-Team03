package com.coffee.domain.order.dto;

import com.coffee.domain.order.entity.Order;
import com.coffee.domain.product.dto.ProductDto;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long orderId;
    private Long productId;
    private String customerEmail;
    private LocalDateTime orderDate;
    private Boolean orderState;
    private Integer quantity;

    // 상품 정보를 담을 DTO 필드
    private ProductDto productDto;
    // 고객 정보를 담을 DTO 필드
    private CustomerDTO customerDto;

    // Entity to DTO
    public static OrderDto from(Order order) {
        OrderDtoBuilder builder = OrderDto.builder()
                .orderId(order.getOrderId())
                .productId(order.getProductId())
                .customerEmail(order.getCustomerEmail())
                .orderDate(order.getOrderDate())
                .orderState(order.getOrderState())
                .quantity(order.getQuantity());

        // Product 정보가 있다면 DTO에 추가
        if (order.getProduct() != null) {
            builder.productDto(ProductDto.from(order.getProduct()));
        }

        // Customer 정보가 있다면 DTO에 추가
        if (order.getCustomer() != null) {
            builder.customerDto(CustomerDTO.from(order.getCustomer())); // customerDto → CustomerDTO
        }

        return builder.build();
    }

    public Order toEntity() {
        return Order.builder()
                .orderId(this.orderId)
                .customerEmail(this.customerEmail)
                .orderDate(this.orderDate)
                .orderState(this.orderState)
                .quantity(this.quantity)
                .build();
    }
}
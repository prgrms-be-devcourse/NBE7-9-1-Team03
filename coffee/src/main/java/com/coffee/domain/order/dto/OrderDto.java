package com.coffee.domain.order.dto;

import com.coffee.domain.customer.dto.CustomerDto;
import com.coffee.domain.order.entity.Order;
import com.coffee.domain.product.dto.ProductDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {

    private Long orderId;
    @NotNull(message = "상품 ID는 필수")
    private Long productId;
    @NotBlank(message = "고객 이메일은 필수 입력 항목입니다.")
    private String customerEmail;
    private LocalDateTime orderDate;
    private Boolean orderState;
    @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
    private Integer quantity;

    // 상품 정보를 담을 DTO 필드
    private ProductDto productDto;
    // 고객 정보를 담을 DTO 필드
    private CustomerDto customerDto;

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
            builder.customerDto(CustomerDto.from(order.getCustomer())); // customerDto → CustomerDTO
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
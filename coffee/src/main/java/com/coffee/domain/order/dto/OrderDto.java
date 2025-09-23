package com.coffee.domain.order.dto;

import com.coffee.domain.order.entity.Order;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    
    private Integer orderId;
    private Integer userId;
    private LocalDateTime orderDay;
    private Boolean orderState;

    
    // Entity to DTO
    public static OrderDto from(Order order) {
        OrderDtoBuilder builder = OrderDto.builder()
                .orderId(order.getOrderId())
                .userId(order.getUserId())
                .orderDay(order.getOrderDay())
                .orderState(order.getOrderState());
        
        return builder.build();
    }
    
    // DTO to Entity
    public Order toEntity() {
        return Order.builder()
                .orderId(this.orderId)
                .userId(this.userId)
                .orderDay(this.orderDay)
                .orderState(this.orderState)
                .build();
    }
}
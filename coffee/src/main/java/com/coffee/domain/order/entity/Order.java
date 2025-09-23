package com.coffee.domain.order.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "`order`") // order는 예약어이므로 백틱으로 감싸기
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Integer orderId;
    
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @Column(name = "order_day", nullable = false)
    private LocalDateTime orderDay;
    
    @Column(name = "order_state", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean orderState = false;
    
    // User와의 다대일 관계 - 느슨한 결합 (User 삭제되어도 Order는 유지)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;
}
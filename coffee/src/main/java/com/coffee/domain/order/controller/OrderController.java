package com.coffee.domain.order.controller;

import com.coffee.domain.order.dto.OrderDto;
import com.coffee.domain.order.service.OrderService;
import com.coffee.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "OrderController", description = "주문 API")
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{customerEmail}")
    @Operation(summary = "특정 고객의 주문 목록 조회")
    public RsData<List<OrderDto>> getOrder(@PathVariable String customerEmail) {
        List<OrderDto> orders = orderService.findByCustomerEmail(customerEmail);

        return new RsData<>(
                "200-1",
                "주문 상세 정보를 조회했습니다.",
                orders
        );
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "주문 취소")
    public RsData<Void> cancelOrder(
            @PathVariable Long orderId
    ) {
        orderService.deleteByOrderId(orderId);

        return new RsData<>(
                "200-1",
                "주문이 취소되었습니다."
        );
    }


    @PutMapping("/{orderId}")
    @Operation(summary = "주문 수정")
    public RsData<Void> updateOrder(
            @RequestBody @Valid OrderDto reqBody,
            @PathVariable Long orderId
    ) {

        // 서비스에서 주문을 생성하고, 주문 시간을 반환받습니다.
        LocalDateTime orderTime = orderService.updateOrder(reqBody);

        // 주문 시간에 따라 메시지를 동적으로 생성합니다.
        String message = (orderTime.getHour() >= 14)
                ? "당일 오후 2시 이후의 주문 건은 다음 날 배송이 시작됩니다."
                : "수정이 완료되었습니다. 배송은 금일 시작됩니다.";

        return new RsData<>(
                "200-1",
                message,
                null
        );
    }

    @PostMapping("")
    @Operation(summary = "주문")
    public RsData<Void> createOrder(@RequestBody @Valid OrderDto reqBody) {

        // 서비스에서 주문을 생성하고, 주문 시간을 반환받습니다.
        LocalDateTime orderTime = orderService.createOrder(reqBody);

        // 주문 시간에 따라 메시지를 동적으로 생성합니다.
        String message = (orderTime.getHour() >= 14)
                ? "당일 오후 2시 이후의 주문 건은 다음 날 배송이 시작됩니다."
                : "주문이 완료되었습니다. 배송은 금일 시작됩니다.";

        return new RsData<>(
                "201-1",
                message,
                null
        );
    }

}
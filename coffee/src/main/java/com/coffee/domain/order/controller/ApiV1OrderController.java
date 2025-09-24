package com.coffee.domain.order.controller;

import com.coffee.domain.order.dto.OrderDto;
import com.coffee.domain.order.product.dto.OrderedProductDto;
import com.coffee.domain.order.product.entity.OrderedProduct;
import com.coffee.domain.order.product.service.OrderProductService;
import com.coffee.domain.order.service.OrderService;
import com.coffee.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
@Tag(name = "ApiV1CommentController", description = "주문 API")
public class ApiV1OrderController {

    private final OrderService orderService;
    private final OrderProductService orderProductService;

    @GetMapping(value = "/{orderId}/order")
    @Operation(summary = "특정 주문의 주문상품 목록 조회")
    public List<OrderedProductDto> getOrderedProducts(
            @PathVariable Long orderId
    ) {
        // orderId로 해당 주문의 모든 주문상품들 조회
        List<OrderedProduct> orderedProducts = orderProductService.findByOrderId(orderId);

        return orderedProducts.stream()
                .map(OrderedProductDto::from)
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{orderId}/orderProduct/{orderedProductId}")
    @Operation(summary = "주문 삭제")
    public RsData<Void> deleteItem(
            @PathVariable Long orderId,
            @PathVariable Long orderedProductId
    ) {
        orderProductService.deleteById(orderedProductId,orderId);

        return new RsData<>(
                "200-1",
                "%d번 주문건이 삭제되었습니다.".formatted(orderedProductId)
        );
    }

    @PostMapping("/")
    @Transactional
    @Operation(summary = "주문 생성")
    public RsData<Void> createItem(
            @RequestParam Long memberId,
            @RequestBody List<OrderedProductDto> reqBody ) {


        return new RsData<>(
                "201-1",
                "%d번 주문이 완료되었습니다.".formatted(orderedProducts.getId()),
                OrderDto.from(orderedProducts)
        );
    }
}
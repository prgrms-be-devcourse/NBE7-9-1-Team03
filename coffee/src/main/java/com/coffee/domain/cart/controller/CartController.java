package com.coffee.domain.cart.controller;

import com.coffee.domain.cart.service.RedisCartService;
import com.coffee.domain.order.dto.OrderDto;
import com.coffee.global.rsData.RsData;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final RedisCartService redisCartService;

    @Autowired
    public CartController(RedisCartService redisCartService) {
        this.redisCartService = redisCartService;
    }

    // 1. 장바구니에 상품 추가/수정 (POST)
    // URL: /cart/add
    @PostMapping("/add")
    public RsData<Void> addProductToCart(@RequestBody @Valid OrderDto reqBody) {
        redisCartService.addProductToCart(
                reqBody.getCustomerEmail(),
                String.valueOf(reqBody.getProductId()),
                reqBody.getQuantity());

        return new RsData<>(
                "201-1",
                "상품이 장바구니에 추가되었습니다.",
                null
        );
    }

    // 2. 장바구니 전체 조회 (GET)
    @GetMapping("/{customerEmail}")
    public RsData<List<OrderDto>> getCart(@PathVariable String customerEmail) {
        List<OrderDto> cart = redisCartService.getCart(customerEmail);
        return new RsData<>(
                "200-1",
                "장바구니 목록을 불러왔습니다.",
                cart
        );
    }

    // 3. 장바구니에서 상품 삭제 (DELETE)
    // URL: /cart/remove
    @DeleteMapping("/remove")
    public RsData<Void> removeProductFromCart(@RequestBody @Valid OrderDto reqBody) {
        redisCartService.removeProductFromCart(
                reqBody.getCustomerEmail(),
                String.valueOf(reqBody.getProductId()));

        return new RsData<>(
                "200-1",
                "상품이 장바구니에서 삭제되었습니다.",
                null
        );
    }

    // 4. 장바구니 전체 비우기 (DELETE)
    // URL: /cart/clear
    @DeleteMapping("/clear")
    public RsData<Void> clearCart(@RequestBody OrderDto reqBody) {

        redisCartService.clearCart(reqBody.getCustomerEmail());

        return new RsData<>(
                "200-1",
                "장바구니를 비웠습니다.",
                null
        );
    }
}
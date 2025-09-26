package com.coffee.domain.cart.service;

import com.coffee.domain.order.dto.OrderDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RedisCartService {

    private static final String CART_KEY_PREFIX = "cart:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public RedisCartService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private ListOperations<String, Object> listOperations() {
        return redisTemplate.opsForList();
    }

    private String getCartKey(String customerEmail) {
        return CART_KEY_PREFIX + customerEmail;
    }

    // 1. 장바구니에 상품 추가
    public void addProductToCart(String customerEmail, String productId, int quantity) {
        String cartKey = getCartKey(customerEmail);

        // OrderDto 객체를 직접 저장
        OrderDto productInfo = OrderDto.builder()
                .customerEmail(customerEmail)
                .productId(Long.valueOf(productId))
                .quantity(quantity)
                .build();

        // 기존 상품이 있는지 확인하고 제거
        removeProductFromCart(customerEmail, productId);

        // 새로운 상품 정보 추가
        listOperations().leftPush(cartKey, productInfo);
    }

    // 2. 장바구니 전체 조회
    public List<OrderDto> getCart(String customerEmail) {
        String cartKey = getCartKey(customerEmail);

        List<Object> productList = listOperations().range(cartKey, 0, -1);

        if (productList == null) {
            return List.of();
        }

        return productList.stream()
                .map(obj -> (OrderDto) obj)
                .collect(Collectors.toList());
    }

    // 3. 장바구니에서 상품 삭제
    public void removeProductFromCart(String customerEmail, String productId) {
        String cartKey = getCartKey(customerEmail);

        List<Object> allItems = listOperations().range(cartKey, 0, -1);
        if (allItems == null || allItems.isEmpty()) {
            return;
        }

        // 삭제할 항목 찾기
        for (Object item : allItems) {
            if (item instanceof OrderDto) {
                OrderDto orderDto = (OrderDto) item;
                if (orderDto.getProductId().equals(Long.valueOf(productId))) {
                    listOperations().remove(cartKey, 1, item);
                    break;
                }
            }
        }
    }

    // 4. 장바구니 비우기
    public void clearCart(String customerEmail) {
        String cartKey = getCartKey(customerEmail);
        redisTemplate.delete(cartKey);
    }
}
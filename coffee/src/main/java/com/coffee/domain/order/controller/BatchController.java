package com.coffee.domain.order.controller;

import com.coffee.domain.order.service.OrderBatchService;
import com.coffee.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/batch")
@Tag(name = "BatchController", description = "배치 관리 API (관리자 전용)")
public class BatchController {

    private final OrderBatchService orderBatchService;

    @PostMapping("/orders/process")
    @Operation(summary = "주문 일괄 처리 수동 실행")
    public RsData<String> processOrdersManually(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime endTime
    ) {
        int processedCount = orderBatchService.processOrdersManually(startTime, endTime);
        
        return new RsData<>(
                "200-1",
                "주문 일괄 처리가 완료되었습니다.",
                processedCount + "건 처리됨"
        );
    }

    @GetMapping("/orders/pending-count")
    @Operation(summary = "처리 대기 중인 주문 수 조회")
    public RsData<Long> getPendingOrderCount() {
        long count = orderBatchService.getPendingOrderCount();
        
        return new RsData<>(
                "200-1",
                "처리 대기 중인 주문 수를 조회했습니다.",
                count
        );
    }

    @PostMapping("/orders/process-today")
    @Operation(summary = "오늘의 주문 일괄 처리 (테스트용)")
    public RsData<String> processTodayOrders() {
        // 테스트를 위해 현재 시간 기준으로 처리
        orderBatchService.processOrders();
        
        return new RsData<>(
                "200-1",
                "오늘의 주문 일괄 처리가 실행되었습니다.",
                null
        );
    }
}
package com.coffee.domain.order.service;

import com.coffee.domain.order.entity.Order;
import com.coffee.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBatchService {

    private final OrderRepository orderRepository;

    /**
     * 매일 오후 2시에 실행되는 배치
     * 전날 오후 2시 ~ 금일 오후 2시까지의 주문을 일괄 처리
     */
    @Scheduled(cron = "0 0 14 * * *") // 매일 14:00:00 실행
    @Transactional
    public void processOrders() {
        log.info("주문 일괄 처리 배치 시작");

        // 오늘 14시 기준 컷오프
        LocalDateTime cutoffToday = LocalDate.now().atTime(14, 0);
        // 전날 14시
        LocalDateTime cutoffYesterday = cutoffToday.minusDays(1);

        log.info("처리 대상 기간: {} ~ {}", cutoffYesterday, cutoffToday);

        try {
            // 처리 대상 주문 조회 (로그용)
            List<Order> targetOrders = orderRepository.findByOrderDateBetweenAndOrderState(
                    cutoffYesterday, cutoffToday, false
            );

            log.info("처리 대상 주문 수: {}", targetOrders.size());

            if (targetOrders.isEmpty()) {
                log.info("처리할 주문이 없습니다.");
                return;
            }

            // 주문 상태 일괄 업데이트
            int updatedCount = orderRepository.updateOrderStateByDateRange(cutoffYesterday, cutoffToday);

            log.info("주문 일괄 처리 완료: {}건 처리됨", updatedCount);

            // 처리된 주문들의 상세 정보 로그 (옵션)
            if (log.isDebugEnabled()) {
                targetOrders.forEach(order ->
                        log.debug("처리된 주문 - ID: {}, 고객: {}, 주문시간: {}",
                                order.getOrderId(),
                                order.getCustomerEmail(),
                                order.getOrderDate())
                );
            }

        } catch (Exception e) {
            log.error("주문 일괄 처리 중 오류 발생", e);
            throw e; // 트랜잭션 롤백을 위해 예외 재발생
        }
    }

    /**
     * 수동 실행용 메소드 (테스트 또는 관리자용)
     */
    @Transactional
    public int processOrdersManually(LocalDateTime startTime, LocalDateTime endTime) {
        log.info("수동 주문 처리 시작: {} ~ {}", startTime, endTime);

        List<Order> targetOrders = orderRepository.findByOrderDateBetweenAndOrderState(
                startTime, endTime, false
        );

        if (targetOrders.isEmpty()) {
            log.info("처리할 주문이 없습니다.");
            return 0;
        }

        int updatedCount = orderRepository.updateOrderStateByDateRange(startTime, endTime);
        log.info("수동 주문 처리 완료: {}건", updatedCount);

        return updatedCount;
    }

    /**
     * 현재 처리 대기 중인 주문 수 조회
     */
    public long getPendingOrderCount() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusDays(1).with(LocalTime.of(14, 0, 1));
        LocalDateTime endTime = now.with(LocalTime.of(14, 0, 0));

        return orderRepository.findByOrderDateBetweenAndOrderState(startTime, endTime, false).size();
    }
}
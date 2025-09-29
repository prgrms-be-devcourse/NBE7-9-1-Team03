package com.coffee.domain.customer.service;

import com.coffee.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerPurgeScheduler {

    private final CustomerService customerService;

    //@Scheduled(cron = "0 0 3 * * ?") // 매일 새벽 3시
    @Scheduled(cron = "*/10 * * * * ?") // 테스트 -> 10초마다
    public void purge() {
        try {
            customerService.purgeDeletedCustomers();
        } catch (ServiceException e) {
            log.warn("삭제 실패: {}", e.getMessage());
        }
    }
}

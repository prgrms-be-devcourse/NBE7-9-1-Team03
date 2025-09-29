package com.coffee.domain.customer.service;

import com.coffee.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerPurgeScheduler {

    private final CustomerService customerService;

    @Value("${custom.customer.scheduler.cron}")
    private String cron;

    @Scheduled(cron = "${custom.customer.scheduler.cron}") // 매일 새벽 3시
    public void purge() {
        try {
            customerService.purgeDeletedCustomers();
        } catch (ServiceException e) {
            log.warn("스케줄러: 삭제 실패: {}", e.getMessage());
        }
    }
}
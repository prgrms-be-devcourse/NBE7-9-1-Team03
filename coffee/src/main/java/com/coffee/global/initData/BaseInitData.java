package com.coffee.global.initData;

import com.coffee.domain.user.entity.Customer;
import com.coffee.domain.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {

    @Autowired
    @Lazy
    private BaseInitData self;
    private final CustomerService customerService;

    @Bean
    ApplicationRunner initDataRunner() {
        return args -> {
            self.work1();
            self.work2();
        };

    }

    @Transactional
    public void work1() {
        if(customerService.count() > 0){
            return;
        }
        Customer customer1 = customerService
                .join("vectorh532@gmail.com", "1234", "정한영", "안드로메다", 12345);
    }

    @Transactional
    public void work2() {
    }
}
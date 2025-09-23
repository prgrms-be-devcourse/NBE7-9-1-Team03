package com.coffee.domain.user.service;

import com.coffee.domain.user.entity.Customer;
import com.coffee.domain.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findById(long id){
        return customerRepository.findById(id);
    }

    public List<Customer> findAll(){
        return customerRepository.findAll();
    }

    // 이메일 기준 이미 고객이 있으면 새로운 입력기준 update 후 기존 고객정보 반환,
    // 없다면 새로 고객정보 저장
    @Transactional
    public Customer join(String email, String username, String address, Integer postalCode) {
        return customerRepository.findByEmail(email)
                .map(existing -> {
                    existing.updateInfo(username, address, postalCode); // 더티체킹으로 update sql
                    return existing;
                })
                .orElseGet(() -> {
                    Customer newCustomer = new Customer(email, username, address, postalCode);
                    return customerRepository.save(newCustomer);
                });
    }
}

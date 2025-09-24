package com.coffee.domain.user.service;

import com.coffee.domain.user.entity.Customer;
import com.coffee.domain.user.repository.CustomerRepository;
import com.coffee.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public Customer join(String email, String password, String username, String address, Integer postalCode) {
        Customer customer = new Customer(email, password, username, address, postalCode);

        findByEmail(email).ifPresent(customer1 -> {
            throw new ServiceException("401", "이미 사용중인 이메일입니다");
        });

        return customerRepository.save(customer);
    }




    public void checkPassword(String inputPass, String rawPass){
        if(!inputPass.equals(rawPass)){
            throw new ServiceException("401", "비밀번호가 일치하지 않습니다");
        }
    }

    public Optional<Customer> findByApiKey(String apiKey) {
        return customerRepository.findByApiKey(apiKey);
    }
}

/*
@Transactional
    public Customer join(String email, String password, String username, String address, Integer postalCode) {
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
 */
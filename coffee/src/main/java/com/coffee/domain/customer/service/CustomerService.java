// 변경사항 원복
package com.coffee.domain.customer.service;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.repository.CustomerRepository;
import com.coffee.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;


    public long count() {
        return customerRepository.count();}

    public Optional<Customer> findByEmail(String email) {
        return customerRepository.findByEmail(email);
    }

    public Optional<Customer> findById(long id){
        return customerRepository.findById(id);
    }

    public List<Customer> findAll(){
        return customerRepository.findAll();
    }

    @Transactional
    public Customer join(String email, String rawpassword, String username, String address, Integer postalCode) {
        findByEmail(email).ifPresent(customer1 -> {
            throw new ServiceException("401", "이미 사용중인 이메일입니다");
        });

        String encodedPassword = passwordEncoder.encode(rawpassword);

        Customer customer = new Customer(email, encodedPassword, username, address, postalCode);
        customer.updateRefreshToken(authService.genRefreshToken(customer));     // refresh토큰 설정

        return customerRepository.save(customer);
    }

    @Transactional
    public Customer login(String email, String password){
        Customer customer = findByEmail(email).orElseThrow(
                () -> new ServiceException("401", "존재하지 않는 아이디 입니다.")
        );
        checkPassword(password, customer.getPassword());

        customer.updateRefreshToken(authService.genRefreshToken(customer));
        return customerRepository.save(customer);
    }

    @Transactional
    public void logout(Customer customer) {
        // RefreshToken 제거
        customer.clearRefreshToken();
        customerRepository.save(customer);
    }

    public void checkPassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new ServiceException("401", "비밀번호가 일치하지 않습니다");
        }
    }

    @Transactional
    public void modifyMe(Customer customer, String username, String address, Integer postalCode) {
        customer.updateInfo(username, address, postalCode);     // JPA 더티체킹으로 update
    }

    @Transactional
    public void quit(Customer actor) {
        customerRepository.delete(actor);
    }
}
// 변경사항 원복
package com.coffee.domain.customer.service;

import com.coffee.domain.customer.entity.Customer;
import com.coffee.domain.customer.repository.CustomerRepository;
import com.coffee.domain.order.repository.OrderRepository;
import com.coffee.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final OrderRepository orderRepository;

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
    public void quit(Customer customer) {
        customer.markDeleted();
    }
    // 탈퇴 사용자(quit으로 deleted=true로 마크된 사용자)
    // && 탈퇴상태가 threshold이상 경과된 사용자 삭제 로직
    // soft delete: CusomterPurgeScheduler로 마크된 사용자 스케쥴링에 의해 일괄 삭제
    @Transactional
    public void purgeDeletedCustomers() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(15); // 테스트용: 삭제일이 15초 지난 사용자 삭제
        List<Customer> targets = customerRepository.findPurgeTargets(threshold);

        for (Customer customer : targets) {
            long orderCount = orderRepository.countByCustomerEmail(customer.getEmail());
            if(orderCount > 0) {
                throw new ServiceException("401", "주문내역이 있는 고객입니다");
            }
            customerRepository.delete(customer);
        }
        log.info("Deleted {} customer(s) from the DB", targets.size());
    }
}
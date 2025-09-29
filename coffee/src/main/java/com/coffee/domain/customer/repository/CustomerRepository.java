package com.coffee.domain.customer.repository;

import com.coffee.domain.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByEmail(String email);

    @Query("SELECT c FROM Customer c WHERE c.deleted = true AND c.deletedAt < :threshold")
    List<Customer> findPurgeTargets(LocalDateTime threshold);
}

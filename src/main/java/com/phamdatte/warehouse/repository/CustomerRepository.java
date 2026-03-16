package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    @Query("SELECT c FROM Customer c WHERE (:keyword IS NULL OR LOWER(c.customerName) LIKE LOWER(CONCAT('%',:keyword,'%'))) AND c.isActive = true")
    Page<Customer> findActiveByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByCustomerCode(String customerCode);
}

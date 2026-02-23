package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VendorRepository extends JpaRepository<Vendor, Integer> {

    @Query("SELECT v FROM Vendor v WHERE v.isActive = true AND (:keyword IS NULL OR LOWER(v.vendorName) LIKE LOWER(CONCAT('%',:keyword,'%')))")
    Page<Vendor> findActiveByKeyword(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByVendorCode(String vendorCode);
}

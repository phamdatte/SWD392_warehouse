package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.InventoryTransaction;
import com.phamdatte.warehouse.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Integer> {

    @Query("""
        SELECT t FROM InventoryTransaction t
        WHERE (:productId IS NULL OR t.product.productId = :productId)
          AND (:type IS NULL OR t.transactionType = :type)
          AND (:from IS NULL OR t.transactionDate >= :from)
          AND (:to IS NULL OR t.transactionDate <= :to)
        ORDER BY t.transactionDate DESC
    """)
    Page<InventoryTransaction> findByFilter(
            @Param("productId") Integer productId,
            @Param("type") TransactionType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);
}

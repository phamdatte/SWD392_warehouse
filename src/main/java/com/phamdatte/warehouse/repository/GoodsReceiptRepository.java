package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsReceipt;
import com.phamdatte.warehouse.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Integer> {

    @Query("""
        SELECT gr FROM GoodsReceipt gr
        WHERE (:status IS NULL OR gr.status = :status)
          AND (:from IS NULL OR gr.receiptDate >= :from)
          AND (:to IS NULL OR gr.receiptDate <= :to)
        ORDER BY gr.createdAt DESC
    """)
    Page<GoodsReceipt> findByFilter(
            @Param("status") ReceiptStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    boolean existsByReceiptNumber(String receiptNumber);
}

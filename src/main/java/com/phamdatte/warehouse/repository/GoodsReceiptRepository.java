package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsReceipt;
import com.phamdatte.warehouse.enums.ReceiptStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Integer> {

    @Query(value = """
        SELECT gr FROM GoodsReceipt gr
        LEFT JOIN FETCH gr.vendor
        LEFT JOIN FETCH gr.createdBy
        LEFT JOIN FETCH gr.approvedBy
        LEFT JOIN FETCH gr.items i
        LEFT JOIN FETCH i.product
        WHERE (:status IS NULL OR gr.status = :status)
          AND (:from IS NULL OR gr.receiptDate >= :from)
          AND (:to IS NULL OR gr.receiptDate <= :to)
    """,
    countQuery = """
        SELECT COUNT(gr) FROM GoodsReceipt gr
        WHERE (:status IS NULL OR gr.status = :status)
          AND (:from IS NULL OR gr.receiptDate >= :from)
          AND (:to IS NULL OR gr.receiptDate <= :to)
    """)
    Page<GoodsReceipt> findByFilter(
            @Param("status") ReceiptStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    // JOIN FETCH để tránh LazyInitializationException khi gọi getById
    @Query("""
        SELECT gr FROM GoodsReceipt gr
        LEFT JOIN FETCH gr.vendor
        LEFT JOIN FETCH gr.createdBy
        LEFT JOIN FETCH gr.approvedBy
        LEFT JOIN FETCH gr.items i
        LEFT JOIN FETCH i.product
        WHERE gr.receiptId = :id
    """)
    Optional<GoodsReceipt> findByIdWithDetails(@Param("id") Integer id);

    boolean existsByReceiptNumber(String receiptNumber);
}

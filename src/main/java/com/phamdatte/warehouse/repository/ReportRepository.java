package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsReceipt;
import com.phamdatte.warehouse.enums.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<GoodsReceipt, Integer> {

    @Query("""
        SELECT gr FROM GoodsReceipt gr
        WHERE gr.status = 'Approved'
          AND gr.approvedAt >= :from AND gr.approvedAt <= :to
    """)
    List<GoodsReceipt> findApprovedReceiptsByPeriod(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}

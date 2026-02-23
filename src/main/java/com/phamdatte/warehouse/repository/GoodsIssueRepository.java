package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsIssue;
import com.phamdatte.warehouse.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface GoodsIssueRepository extends JpaRepository<GoodsIssue, Integer> {

    @Query("""
        SELECT gi FROM GoodsIssue gi
        WHERE (:status IS NULL OR gi.status = :status)
          AND (:from IS NULL OR gi.issueDate >= :from)
          AND (:to IS NULL OR gi.issueDate <= :to)
        ORDER BY gi.createdAt DESC
    """)
    Page<GoodsIssue> findByFilter(
            @Param("status") IssueStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    boolean existsByIssueNumber(String issueNumber);
}

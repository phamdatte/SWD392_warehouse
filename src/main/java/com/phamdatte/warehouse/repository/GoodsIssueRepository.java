package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsIssue;
import com.phamdatte.warehouse.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface GoodsIssueRepository extends JpaRepository<GoodsIssue, Integer> {

    @Query(value = """
        SELECT gi FROM GoodsIssue gi
        LEFT JOIN FETCH gi.customer
        LEFT JOIN FETCH gi.createdBy
        LEFT JOIN FETCH gi.approvedBy
        LEFT JOIN FETCH gi.items i
        LEFT JOIN FETCH i.product
        WHERE (:status IS NULL OR gi.status = :status)
          AND (:from IS NULL OR gi.issueDate >= :from)
          AND (:to IS NULL OR gi.issueDate <= :to)
    """,
    countQuery = """
        SELECT COUNT(gi) FROM GoodsIssue gi
        WHERE (:status IS NULL OR gi.status = :status)
          AND (:from IS NULL OR gi.issueDate >= :from)
          AND (:to IS NULL OR gi.issueDate <= :to)
    """)
    Page<GoodsIssue> findByFilter(
            @Param("status") IssueStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            Pageable pageable);

    @Query("""
        SELECT gi FROM GoodsIssue gi
        LEFT JOIN FETCH gi.customer
        LEFT JOIN FETCH gi.createdBy
        LEFT JOIN FETCH gi.approvedBy
        LEFT JOIN FETCH gi.items i
        LEFT JOIN FETCH i.product
        WHERE gi.issueId = :id
    """)
    Optional<GoodsIssue> findByIdWithDetails(@Param("id") Integer id);

    boolean existsByIssueNumber(String issueNumber);
}

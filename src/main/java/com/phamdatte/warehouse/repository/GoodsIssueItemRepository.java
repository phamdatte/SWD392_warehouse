package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsIssueItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsIssueItemRepository extends JpaRepository<GoodsIssueItem, Integer> {
    List<GoodsIssueItem> findByIssueIssueId(Integer issueId);

    @Modifying
    @Query("DELETE FROM GoodsIssueItem i WHERE i.issue.issueId = :issueId")
    void deleteByIssueIssueId(@Param("issueId") Integer issueId);
}

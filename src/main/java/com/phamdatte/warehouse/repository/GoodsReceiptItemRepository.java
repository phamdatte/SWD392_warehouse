package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Integer> {
    List<GoodsReceiptItem> findByReceiptReceiptId(Integer receiptId);

    @Modifying
    @Query("DELETE FROM GoodsReceiptItem i WHERE i.receipt.receiptId = :receiptId")
    void deleteByReceiptReceiptId(@Param("receiptId") Integer receiptId);
}

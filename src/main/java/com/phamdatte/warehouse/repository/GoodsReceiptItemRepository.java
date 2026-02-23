package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, Integer> {
    List<GoodsReceiptItem> findByReceiptReceiptId(Integer receiptId);
}

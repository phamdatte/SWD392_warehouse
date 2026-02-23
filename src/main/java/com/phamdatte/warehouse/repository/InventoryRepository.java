package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    Optional<Inventory> findByProductProductId(Integer productId);
}

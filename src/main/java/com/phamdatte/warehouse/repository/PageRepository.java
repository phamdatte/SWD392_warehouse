package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepository extends JpaRepository<Page, Integer> {
    boolean existsByPageCode(String pageCode);
    List<Page> findAllByIsActiveTrueOrderByDisplayOrder();
}

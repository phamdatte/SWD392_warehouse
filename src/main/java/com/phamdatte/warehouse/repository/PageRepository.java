package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.Page;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PageRepository extends JpaRepository<Page, Integer> {
    boolean existsByPageCode(String pageCode);
}

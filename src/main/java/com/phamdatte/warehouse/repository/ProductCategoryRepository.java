package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
    boolean existsByCategoryName(String categoryName);
}

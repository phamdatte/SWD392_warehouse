package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Integer> {

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category
        WHERE (:keyword IS NULL OR LOWER(p.productName) LIKE LOWER(CONCAT('%',:keyword,'%'))
               OR LOWER(p.productCode) LIKE LOWER(CONCAT('%',:keyword,'%')))
          AND (:categoryId IS NULL OR (p.category IS NOT NULL AND p.category.categoryId = :categoryId))
          AND (:activeOnly IS NULL OR :activeOnly = false OR p.isActive = true)
    """)
    Page<Product> findByFilter(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("activeOnly") Boolean activeOnly,
            Pageable pageable);

    boolean existsByProductCode(String productCode);
    boolean existsByBarcode(String barcode);
}

package com.phamdatte.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_category")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryId;

    @Column(nullable = false, unique = true, length = 50)
    private String categoryName;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;
}

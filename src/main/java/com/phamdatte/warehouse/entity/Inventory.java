package com.phamdatte.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Inventory {

    @Id
    private Integer productId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column
    private LocalDateTime lastUpdated;
}

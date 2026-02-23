package com.phamdatte.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "goods_issue_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoodsIssueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issueItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private GoodsIssue issue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;
}

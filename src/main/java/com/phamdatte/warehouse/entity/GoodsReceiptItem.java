package com.phamdatte.warehouse.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "goods_receipt_item")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer receiptItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private GoodsReceipt receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // subtotal là GENERATED ALWAYS AS — ánh xạ insertable=false, updatable=false
    @Column(precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;
}

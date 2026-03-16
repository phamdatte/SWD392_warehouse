package com.phamdatte.warehouse.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class InventoryResponse {
    private Integer productId;
    private String productCode;
    private String barcode;
    private String productName;
    private String categoryName;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal currentQuantity;
    private BigDecimal inventoryValue;
    private BigDecimal totalReceipt;
    private BigDecimal totalIssue;
    private LocalDateTime lastUpdated;
}

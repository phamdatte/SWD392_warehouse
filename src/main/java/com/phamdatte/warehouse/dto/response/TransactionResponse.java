package com.phamdatte.warehouse.dto.response;

import com.phamdatte.warehouse.enums.ReferenceType;
import com.phamdatte.warehouse.enums.TransactionType;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class TransactionResponse {
    private Integer transactionId;
    private Integer productId;
    private String productCode;
    private String productName;
    private TransactionType transactionType;
    private BigDecimal quantity;
    private BigDecimal quantityBefore;
    private BigDecimal quantityAfter;
    private Integer referenceId;
    private ReferenceType referenceType;
    private String notes;
    private String performedBy;
    private LocalDateTime transactionDate;
}

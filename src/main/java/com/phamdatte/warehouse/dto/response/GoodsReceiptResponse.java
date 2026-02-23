package com.phamdatte.warehouse.dto.response;

import com.phamdatte.warehouse.enums.ReceiptStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class GoodsReceiptResponse {
    private Integer receiptId;
    private String receiptNumber;
    private String vendorName;
    private Integer vendorId;
    private LocalDateTime receiptDate;
    private ReceiptStatus status;
    private String notes;
    private String createdByName;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<GoodsReceiptItemResponse> items;

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class GoodsReceiptItemResponse {
        private Integer receiptItemId;
        private Integer productId;
        private String productCode;
        private String productName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}

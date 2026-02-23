package com.phamdatte.warehouse.dto.response;

import com.phamdatte.warehouse.enums.IssueStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class GoodsIssueResponse {
    private Integer issueId;
    private String issueNumber;
    private Integer customerId;
    private String customerName;
    private LocalDateTime issueDate;
    private IssueStatus status;
    private String notes;
    private String createdByName;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private LocalDateTime createdAt;
    private BigDecimal totalAmount;
    private List<GoodsIssueItemResponse> items;

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class GoodsIssueItemResponse {
        private Integer issueItemId;
        private Integer productId;
        private String productCode;
        private String productName;
        private String unit;
        private BigDecimal quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
        private BigDecimal currentStock; // tồn kho hiện tại — tiện hiển thị
    }
}

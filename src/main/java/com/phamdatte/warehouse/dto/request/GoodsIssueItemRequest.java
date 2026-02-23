package com.phamdatte.warehouse.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class GoodsIssueItemRequest {

    @NotNull
    private Integer productId;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal quantity;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal unitPrice;
}

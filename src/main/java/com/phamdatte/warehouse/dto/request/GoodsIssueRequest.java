package com.phamdatte.warehouse.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class GoodsIssueRequest {

    private Integer customerId; // nullable

    @NotNull
    private LocalDateTime issueDate;

    private String notes;

    @NotEmpty
    @Valid
    private List<GoodsIssueItemRequest> items;
}

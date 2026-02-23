package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.response.GoodsIssueResponse;
import com.phamdatte.warehouse.dto.response.GoodsReceiptResponse;
import com.phamdatte.warehouse.dto.response.InventoryResponse;
import com.phamdatte.warehouse.enums.IssueStatus;
import com.phamdatte.warehouse.enums.ReceiptStatus;
import com.phamdatte.warehouse.service.GoodsIssueService;
import com.phamdatte.warehouse.service.GoodsReceiptService;
import com.phamdatte.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('Manager', 'Admin')")
@RequiredArgsConstructor
public class ReportController {

    private final GoodsReceiptService receiptService;
    private final GoodsIssueService issueService;
    private final InventoryService inventoryService;

    // UC15 - Báo cáo nhập kho theo kỳ
    @GetMapping("/receipt")
    public ResponseEntity<Page<GoodsReceiptResponse>> receiptReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("receiptDate").descending());
        return ResponseEntity.ok(receiptService.getAll(ReceiptStatus.Approved, from, to, pageable));
    }

    // UC16 - Báo cáo xuất kho theo kỳ
    @GetMapping("/issue")
    public ResponseEntity<Page<GoodsIssueResponse>> issueReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("issueDate").descending());
        return ResponseEntity.ok(issueService.getAll(IssueStatus.Approved, from, to, pageable));
    }

    // UC17 - Báo cáo tồn kho
    @GetMapping("/inventory")
    public ResponseEntity<Page<InventoryResponse>> inventoryReport(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "500") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("productCode").ascending());
        return ResponseEntity.ok(inventoryService.getCurrentStock(null, null, pageable));
    }
}

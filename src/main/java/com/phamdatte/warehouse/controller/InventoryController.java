package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.response.InventoryResponse;
import com.phamdatte.warehouse.dto.response.TransactionResponse;
import com.phamdatte.warehouse.enums.TransactionType;
import com.phamdatte.warehouse.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // UC13 - Tồn kho hiện tại
    @GetMapping
    public ResponseEntity<Page<InventoryResponse>> getCurrentStock(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @PageableDefault(size = 20, sort = "productCode", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getCurrentStock(keyword, categoryId, pageable));
    }

    // UC14 - Lịch sử giao dịch
    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionResponse>> getTransactions(
            @RequestParam(required = false) Integer productId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(inventoryService.getTransactions(productId, type, from, to, pageable));
    }
}

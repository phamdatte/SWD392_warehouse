package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.request.GoodsReceiptRequest;
import com.phamdatte.warehouse.dto.response.GoodsReceiptResponse;
import com.phamdatte.warehouse.enums.ReceiptStatus;
import com.phamdatte.warehouse.service.GoodsReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService receiptService;

    // UC03 - Danh sách phiếu nhập
    @GetMapping
    public ResponseEntity<Page<GoodsReceiptResponse>> getAll(
            @RequestParam(required = false) ReceiptStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(receiptService.getAll(status, from, to, pageable));
    }

    // UC05 - Chi tiết phiếu nhập
    @GetMapping("/{id}")
    public ResponseEntity<GoodsReceiptResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(receiptService.getById(id));
    }

    // UC04 - Tạo phiếu nhập
    @PostMapping
    public ResponseEntity<GoodsReceiptResponse> create(
            @Valid @RequestBody GoodsReceiptRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(receiptService.create(request, user.getUsername()));
    }

    // UC06 - Sửa phiếu nhập (chỉ khi PENDING)
    @PutMapping("/{id}")
    public ResponseEntity<GoodsReceiptResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody GoodsReceiptRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(receiptService.update(id, request, user.getUsername()));
    }

    // UC07 - Duyệt phiếu nhập (Manager/Admin)
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('Manager', 'Admin')")
    public ResponseEntity<GoodsReceiptResponse> approve(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(receiptService.approve(id, user.getUsername()));
    }

    // Hủy phiếu nhập (chỉ khi Pending)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<GoodsReceiptResponse> cancel(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(receiptService.cancel(id, user.getUsername()));
    }
}

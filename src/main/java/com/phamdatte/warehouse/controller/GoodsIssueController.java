package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.request.GoodsIssueRequest;
import com.phamdatte.warehouse.dto.response.GoodsIssueResponse;
import com.phamdatte.warehouse.enums.IssueStatus;
import com.phamdatte.warehouse.service.GoodsIssueService;
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
@RequestMapping("/api/issues")
@RequiredArgsConstructor
public class GoodsIssueController {

    private final GoodsIssueService issueService;

    // UC08
    @GetMapping
    public ResponseEntity<Page<GoodsIssueResponse>> getAll(
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(issueService.getAll(status, from, to, pageable));
    }

    // UC10
    @GetMapping("/{id}")
    public ResponseEntity<GoodsIssueResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    // UC09
    @PostMapping
    public ResponseEntity<GoodsIssueResponse> create(
            @Valid @RequestBody GoodsIssueRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(issueService.create(req, user.getUsername()));
    }

    // UC11
    @PutMapping("/{id}")
    public ResponseEntity<GoodsIssueResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody GoodsIssueRequest req,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(issueService.update(id, req, user.getUsername()));
    }

    // UC12 - chỉ Manager/Admin
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('Manager', 'Admin')")
    public ResponseEntity<GoodsIssueResponse> approve(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(issueService.approve(id, user.getUsername()));
    }

    // Hủy phiếu xuất (chỉ khi Pending)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<GoodsIssueResponse> cancel(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(issueService.cancel(id, user.getUsername()));
    }
}

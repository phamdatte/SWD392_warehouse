package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.request.GoodsIssueItemRequest;
import com.phamdatte.warehouse.dto.request.GoodsIssueRequest;
import com.phamdatte.warehouse.dto.response.GoodsIssueResponse;
import com.phamdatte.warehouse.entity.*;
import com.phamdatte.warehouse.enums.IssueStatus;
import com.phamdatte.warehouse.enums.ReferenceType;
import com.phamdatte.warehouse.enums.TransactionType;
import com.phamdatte.warehouse.exception.BusinessException;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsIssueService {

    private final GoodsIssueRepository issueRepository;
    private final GoodsIssueItemRepository issueItemRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    // UC08 - Danh sách phiếu xuất
    @Transactional(readOnly = true)
    public Page<GoodsIssueResponse> getAll(IssueStatus status,
                                            LocalDateTime from,
                                            LocalDateTime to,
                                            Pageable pageable) {
        return issueRepository.findByFilter(status, from, to, pageable).map(this::toResponse);
    }

    // UC10 - Chi tiết phiếu xuất
    @Transactional(readOnly = true)
    public GoodsIssueResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    // UC09 - Tạo phiếu xuất
    @Transactional
    public GoodsIssueResponse create(GoodsIssueRequest req, String username) {
        User createdBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GoodsIssue issue = GoodsIssue.builder()
                .issueNumber(generateIssueNumber())
                .issueDate(req.getIssueDate())
                .notes(req.getNotes())
                .status(IssueStatus.Pending)
                .createdBy(createdBy)
                .build();

        if (req.getCustomerId() != null) {
            Customer customer = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            issue.setCustomer(customer);
        }

        addItems(issue, req.getItems());
        return toResponse(issueRepository.save(issue));
    }

    // UC11 - Sửa phiếu xuất (chỉ khi PENDING)
    @Transactional
    public GoodsIssueResponse update(Integer id, GoodsIssueRequest req, String username) {
        // Dùng findById thường (không JOIN FETCH items) để tránh Hibernate cache conflict
        GoodsIssue issue = issueRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsIssue not found: " + id));

        if (issue.getStatus() != IssueStatus.Pending) {
            throw new BusinessException("Only PENDING issues can be edited");
        }

        if (req.getCustomerId() != null) {
            Customer customer = customerRepository.findById(req.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
            issue.setCustomer(customer);
        } else {
            issue.setCustomer(null);
        }
        issue.setIssueDate(req.getIssueDate());
        issue.setNotes(req.getNotes());

        // Xóa items cũ bằng JPQL rồi flush() để đảm bảo DELETE xuống DB trước khi INSERT mới
        // (tránh vi phạm UNIQUE constraint (issue_id, product_id))
        issueItemRepository.deleteByIssueIssueId(id);
        issueItemRepository.flush();

        issue.getItems().clear();
        addItems(issue, req.getItems());

        return toResponse(issueRepository.save(issue));
    }

    // Hủy phiếu xuất (chỉ khi PENDING)
    @Transactional
    public GoodsIssueResponse cancel(Integer id, String username) {
        GoodsIssue issue = findOrThrow(id);

        if (issue.getStatus() != IssueStatus.Pending) {
            throw new BusinessException("Only PENDING issues can be cancelled");
        }

        issue.setStatus(IssueStatus.Cancelled);
        return toResponse(issueRepository.save(issue));
    }

    // UC12 - Duyệt phiếu xuất → kiểm tra và trừ tồn kho
    @Transactional
    public GoodsIssueResponse approve(Integer id, String username) {
        GoodsIssue issue = findOrThrow(id);

        if (issue.getStatus() != IssueStatus.Pending) {
            throw new BusinessException("Only PENDING issues can be approved");
        }

        User approvedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Kiểm tra tồn kho trước khi duyệt
        for (GoodsIssueItem item : issue.getItems()) {
            Inventory inv = inventoryRepository.findByProductProductId(item.getProduct().getProductId())
                    .orElseThrow(() -> new BusinessException(
                            "No inventory record for product: " + item.getProduct().getProductCode()));

            if (inv.getQuantity().compareTo(item.getQuantity()) < 0) {
                throw new BusinessException(
                        "Insufficient stock for product " + item.getProduct().getProductCode()
                        + ". Available: " + inv.getQuantity() + ", Requested: " + item.getQuantity());
            }
        }

        // Trừ tồn kho và ghi transaction
        for (GoodsIssueItem item : issue.getItems()) {
            Inventory inv = inventoryRepository.findByProductProductId(item.getProduct().getProductId()).orElseThrow();

            BigDecimal before = inv.getQuantity();
            BigDecimal after  = before.subtract(item.getQuantity());
            inv.setQuantity(after);
            inv.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inv);

            transactionRepository.save(InventoryTransaction.builder()
                    .product(item.getProduct())
                    .transactionType(TransactionType.Issue)
                    .quantity(item.getQuantity())
                    .quantityBefore(before)
                    .quantityAfter(after)
                    .referenceId(issue.getIssueId())
                    .referenceType(ReferenceType.GoodsIssue)
                    .performedBy(approvedBy)
                    .build());
        }

        issue.setStatus(IssueStatus.Approved);
        issue.setApprovedBy(approvedBy);
        issue.setApprovedAt(LocalDateTime.now());

        return toResponse(issueRepository.save(issue));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private GoodsIssue findOrThrow(Integer id) {
        return issueRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsIssue not found: " + id));
    }

    private void addItems(GoodsIssue issue, List<GoodsIssueItemRequest> items) {
        items.forEach(req -> {
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));
            GoodsIssueItem item = GoodsIssueItem.builder()
                    .issue(issue)
                    .product(product)
                    .quantity(req.getQuantity())
                    .unitPrice(req.getUnitPrice())
                    .build();
            issue.getItems().add(item);
        });
    }

    private String generateIssueNumber() {
        String prefix = "IS" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = issueRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }

    private GoodsIssueResponse toResponse(GoodsIssue gi) {
        BigDecimal total = gi.getItems().stream()
                .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GoodsIssueResponse.GoodsIssueItemResponse> itemResp = gi.getItems().stream()
                .map(i -> {
                    BigDecimal stock = inventoryRepository
                            .findByProductProductId(i.getProduct().getProductId())
                            .map(Inventory::getQuantity).orElse(BigDecimal.ZERO);
                    return GoodsIssueResponse.GoodsIssueItemResponse.builder()
                            .issueItemId(i.getIssueItemId())
                            .productId(i.getProduct().getProductId())
                            .productCode(i.getProduct().getProductCode())
                            .productName(i.getProduct().getProductName())
                            .unit(i.getProduct().getUnit())
                            .quantity(i.getQuantity())
                            .unitPrice(i.getUnitPrice())
                            .subtotal(i.getQuantity().multiply(i.getUnitPrice()))
                            .currentStock(stock)
                            .build();
                }).toList();

        return GoodsIssueResponse.builder()
                .issueId(gi.getIssueId())
                .issueNumber(gi.getIssueNumber())
                .customerId(gi.getCustomer() != null ? gi.getCustomer().getCustomerId() : null)
                .customerName(gi.getCustomer() != null ? gi.getCustomer().getCustomerName() : null)
                .issueDate(gi.getIssueDate())
                .status(gi.getStatus())
                .notes(gi.getNotes())
                .createdByName(gi.getCreatedBy() != null ? gi.getCreatedBy().getFullName() : null)
                .approvedByName(gi.getApprovedBy() != null ? gi.getApprovedBy().getFullName() : null)
                .approvedAt(gi.getApprovedAt())
                .createdAt(gi.getCreatedAt())
                .totalAmount(total)
                .items(itemResp)
                .build();
    }
}

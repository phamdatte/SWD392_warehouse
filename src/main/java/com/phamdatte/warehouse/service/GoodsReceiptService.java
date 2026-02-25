package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.request.GoodsReceiptItemRequest;
import com.phamdatte.warehouse.dto.request.GoodsReceiptRequest;
import com.phamdatte.warehouse.dto.response.GoodsReceiptResponse;
import com.phamdatte.warehouse.entity.*;
import com.phamdatte.warehouse.enums.ReceiptStatus;
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
public class GoodsReceiptService {

    private final GoodsReceiptRepository receiptRepository;
    private final GoodsReceiptItemRepository receiptItemRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;

    // UC03 - Danh sách phiếu nhập
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public Page<GoodsReceiptResponse> getAll(ReceiptStatus status,
                                              LocalDateTime from,
                                              LocalDateTime to,
                                              Pageable pageable) {
        return receiptRepository.findByFilter(status, from, to, pageable)
                .map(this::toResponse);
    }

    // UC05 - Chi tiết phiếu nhập
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public GoodsReceiptResponse getById(Integer id) {
        return toResponse(findOrThrow(id));
    }

    // UC04 - Tạo phiếu nhập
    @Transactional
    public GoodsReceiptResponse create(GoodsReceiptRequest req, String username) {
        Vendor vendor = vendorRepository.findById(req.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));
        User createdBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        GoodsReceipt receipt = GoodsReceipt.builder()
                .receiptNumber(generateReceiptNumber())
                .vendor(vendor)
                .receiptDate(req.getReceiptDate())
                .notes(req.getNotes())
                .status(ReceiptStatus.Pending)
                .createdBy(createdBy)
                .build();

        addItems(receipt, req.getItems());
        return toResponse(receiptRepository.save(receipt));
    }

    // UC06 - Sửa phiếu nhập (chỉ khi PENDING)
    @Transactional
    public GoodsReceiptResponse update(Integer id, GoodsReceiptRequest req, String username) {
        // Dùng findById thường (không JOIN FETCH items) để tránh Hibernate cache conflict
        GoodsReceipt receipt = receiptRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsReceipt not found: " + id));

        if (receipt.getStatus() != ReceiptStatus.Pending) {
            throw new BusinessException("Only PENDING receipts can be edited");
        }

        Vendor vendor = vendorRepository.findById(req.getVendorId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found"));

        receipt.setVendor(vendor);
        receipt.setReceiptDate(req.getReceiptDate());
        receipt.setNotes(req.getNotes());

        // Xóa items cũ bằng JPQL rồi flush() để đảm bảo DELETE xuống DB trước khi INSERT mới
        // (tránh vi phạm UNIQUE constraint (receipt_id, product_id))
        receiptItemRepository.deleteByReceiptReceiptId(id);
        receiptItemRepository.flush();

        receipt.getItems().clear();
        addItems(receipt, req.getItems());

        return toResponse(receiptRepository.save(receipt));
    }

    // Hủy phiếu nhập (chỉ khi PENDING)
    @Transactional
    public GoodsReceiptResponse cancel(Integer id, String username) {
        GoodsReceipt receipt = findOrThrow(id);

        if (receipt.getStatus() != ReceiptStatus.Pending) {
            throw new BusinessException("Only PENDING receipts can be cancelled");
        }

        receipt.setStatus(ReceiptStatus.Cancelled);
        return toResponse(receiptRepository.save(receipt));
    }

    // UC07 - Duyệt phiếu nhập → cập nhật tồn kho
    @Transactional
    public GoodsReceiptResponse approve(Integer id, String username) {
        GoodsReceipt receipt = findOrThrow(id);

        if (receipt.getStatus() != ReceiptStatus.Pending) {
            throw new BusinessException("Only PENDING receipts can be approved");
        }

        User approvedBy = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        for (GoodsReceiptItem item : receipt.getItems()) {
            Inventory inv = inventoryRepository.findByProductProductId(item.getProduct().getProductId())
                    .orElseGet(() -> {
                        Inventory newInv = new Inventory();
                        newInv.setProduct(item.getProduct());
                        newInv.setQuantity(BigDecimal.ZERO);
                        return newInv;
                    });

            BigDecimal before = inv.getQuantity();
            BigDecimal after  = before.add(item.getQuantity());
            inv.setQuantity(after);
            inv.setLastUpdated(LocalDateTime.now());
            inventoryRepository.save(inv);

            // Ghi lịch sử
            transactionRepository.save(InventoryTransaction.builder()
                    .product(item.getProduct())
                    .transactionType(TransactionType.Receipt)
                    .quantity(item.getQuantity())
                    .quantityBefore(before)
                    .quantityAfter(after)
                    .referenceId(receipt.getReceiptId())
                    .referenceType(ReferenceType.GoodsReceipt)
                    .performedBy(approvedBy)
                    .build());
        }

        receipt.setStatus(ReceiptStatus.Approved);
        receipt.setApprovedBy(approvedBy);
        receipt.setApprovedAt(LocalDateTime.now());

        return toResponse(receiptRepository.save(receipt));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private GoodsReceipt findOrThrow(Integer id) {
        return receiptRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("GoodsReceipt not found: " + id));
    }

    private void addItems(GoodsReceipt receipt, List<GoodsReceiptItemRequest> items) {
        items.forEach(req -> {
            Product product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + req.getProductId()));
            GoodsReceiptItem item = GoodsReceiptItem.builder()
                    .receipt(receipt)
                    .product(product)
                    .quantity(req.getQuantity())
                    .unitPrice(req.getUnitPrice())
                    .build();
            receipt.getItems().add(item);
        });
    }

    private String generateReceiptNumber() {
        String prefix = "RC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = receiptRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }

    private GoodsReceiptResponse toResponse(GoodsReceipt r) {
        BigDecimal total = r.getItems().stream()
                .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<GoodsReceiptResponse.GoodsReceiptItemResponse> itemResponses = r.getItems().stream()
                .map(i -> GoodsReceiptResponse.GoodsReceiptItemResponse.builder()
                        .receiptItemId(i.getReceiptItemId())
                        .productId(i.getProduct().getProductId())
                        .productCode(i.getProduct().getProductCode())
                        .productName(i.getProduct().getProductName())
                        .unit(i.getProduct().getUnit())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .subtotal(i.getQuantity().multiply(i.getUnitPrice()))
                        .build())
                .toList();

        return GoodsReceiptResponse.builder()
                .receiptId(r.getReceiptId())
                .receiptNumber(r.getReceiptNumber())
                .vendorId(r.getVendor().getVendorId())
                .vendorName(r.getVendor().getVendorName())
                .receiptDate(r.getReceiptDate())
                .status(r.getStatus())
                .notes(r.getNotes())
                .createdByName(r.getCreatedBy() != null ? r.getCreatedBy().getFullName() : null)
                .approvedByName(r.getApprovedBy() != null ? r.getApprovedBy().getFullName() : null)
                .approvedAt(r.getApprovedAt())
                .createdAt(r.getCreatedAt())
                .totalAmount(total)
                .items(itemResponses)
                .build();
    }
}

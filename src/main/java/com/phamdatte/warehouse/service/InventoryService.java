package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.response.InventoryResponse;
import com.phamdatte.warehouse.dto.response.TransactionResponse;
import com.phamdatte.warehouse.entity.GoodsIssue;
import com.phamdatte.warehouse.entity.GoodsReceipt;
import com.phamdatte.warehouse.enums.ReferenceType;
import com.phamdatte.warehouse.enums.TransactionType;
import com.phamdatte.warehouse.repository.GoodsIssueRepository;
import com.phamdatte.warehouse.repository.GoodsReceiptRepository;
import com.phamdatte.warehouse.repository.InventoryRepository;
import com.phamdatte.warehouse.repository.InventoryTransactionRepository;
import com.phamdatte.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final GoodsReceiptRepository receiptRepository;
    private final GoodsIssueRepository issueRepository;

    // UC13 - Tồn kho hiện tại
    @Transactional(readOnly = true)
    public Page<InventoryResponse> getCurrentStock(String keyword, Integer categoryId, Pageable pageable) {
        return productRepository.findByFilter(keyword, categoryId, null, pageable)
                .map(p -> {
                    var inv = inventoryRepository.findByProductProductId(p.getProductId());
                    BigDecimal qty = inv.map(i -> i.getQuantity()).orElse(BigDecimal.ZERO);

                    BigDecimal totalReceipt = transactionRepository
                            .sumByProductAndType(p.getProductId(), TransactionType.Receipt);
                    BigDecimal totalIssue   = transactionRepository
                            .sumByProductAndType(p.getProductId(), TransactionType.Issue);

                    return InventoryResponse.builder()
                            .productId(p.getProductId())
                            .productCode(p.getProductCode())
                            .barcode(p.getBarcode())
                            .productName(p.getProductName())
                            .categoryName(p.getCategory() != null ? p.getCategory().getCategoryName() : null)
                            .unit(p.getUnit())
                            .unitPrice(p.getUnitPrice())
                            .currentQuantity(qty)
                            .inventoryValue(qty.multiply(p.getUnitPrice()))
                            .totalReceipt(totalReceipt != null ? totalReceipt : BigDecimal.ZERO)
                            .totalIssue(totalIssue   != null ? totalIssue   : BigDecimal.ZERO)
                            .lastUpdated(inv.map(i -> i.getLastUpdated()).orElse(null))
                            .build();
                });
    }


    // UC14 - Lịch sử giao dịch
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getTransactions(Integer productId,
                                                      TransactionType type,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      Pageable pageable) {
        return transactionRepository.findByFilter(productId, type, from, to, pageable)
                .map(t -> {
                    var product = t.getProduct();

                    // Lấy unitPrice và referenceNumber từ phiếu liên quan
                    BigDecimal unitPrice = null;
                    BigDecimal totalAmount = null;
                    String referenceNumber = null;

                    if (t.getReferenceId() != null) {
                        if (t.getReferenceType() == ReferenceType.GoodsReceipt) {
                            GoodsReceipt receipt = receiptRepository.findById(t.getReferenceId()).orElse(null);
                            if (receipt != null) {
                                referenceNumber = receipt.getReceiptNumber();
                                // Tìm unitPrice của sản phẩm trong phiếu
                                var item = receipt.getItems().stream()
                                        .filter(i -> i.getProduct().getProductId().equals(product.getProductId()))
                                        .findFirst().orElse(null);
                                if (item != null) {
                                    unitPrice = item.getUnitPrice();
                                    totalAmount = item.getQuantity().multiply(item.getUnitPrice());
                                }
                            }
                        } else if (t.getReferenceType() == ReferenceType.GoodsIssue) {
                            GoodsIssue issue = issueRepository.findById(t.getReferenceId()).orElse(null);
                            if (issue != null) {
                                referenceNumber = issue.getIssueNumber();
                                var item = issue.getItems().stream()
                                        .filter(i -> i.getProduct().getProductId().equals(product.getProductId()))
                                        .findFirst().orElse(null);
                                if (item != null) {
                                    unitPrice = item.getUnitPrice();
                                    totalAmount = item.getQuantity().multiply(item.getUnitPrice());
                                }
                            }
                        }
                    }

                    return TransactionResponse.builder()
                            .transactionId(t.getTransactionId())
                            .productId(product.getProductId())
                            .productCode(product.getProductCode())
                            .productName(product.getProductName())
                            .unit(product.getUnit())
                            .transactionType(t.getTransactionType())
                            .quantity(t.getQuantity())
                            .quantityBefore(t.getQuantityBefore())
                            .quantityAfter(t.getQuantityAfter())
                            .unitPrice(unitPrice)
                            .totalAmount(totalAmount)
                            .referenceId(t.getReferenceId())
                            .referenceType(t.getReferenceType())
                            .referenceNumber(referenceNumber)
                            .notes(t.getNotes())
                            .performedBy(t.getPerformedBy() != null ? t.getPerformedBy().getFullName() : null)
                            .transactionDate(t.getTransactionDate())
                            .build();
                });
    }
}

package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.response.InventoryResponse;
import com.phamdatte.warehouse.dto.response.TransactionResponse;
import com.phamdatte.warehouse.entity.Product;
import com.phamdatte.warehouse.enums.TransactionType;
import com.phamdatte.warehouse.repository.InventoryRepository;
import com.phamdatte.warehouse.repository.InventoryTransactionRepository;
import com.phamdatte.warehouse.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    // UC13 - Tồn kho hiện tại
    public Page<InventoryResponse> getCurrentStock(String keyword, Integer categoryId, Pageable pageable) {
        return productRepository.findByFilter(keyword, categoryId, pageable)
                .map(p -> {
                    var inv = inventoryRepository.findByProductProductId(p.getProductId());
                    BigDecimal qty = inv.map(i -> i.getQuantity()).orElse(BigDecimal.ZERO);
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
                            .lastUpdated(inv.map(i -> i.getLastUpdated()).orElse(null))
                            .build();
                });
    }

    // UC14 - Lịch sử giao dịch
    public Page<TransactionResponse> getTransactions(Integer productId,
                                                      TransactionType type,
                                                      LocalDateTime from,
                                                      LocalDateTime to,
                                                      Pageable pageable) {
        return transactionRepository.findByFilter(productId, type, from, to, pageable)
                .map(t -> TransactionResponse.builder()
                        .transactionId(t.getTransactionId())
                        .productId(t.getProduct().getProductId())
                        .productCode(t.getProduct().getProductCode())
                        .productName(t.getProduct().getProductName())
                        .transactionType(t.getTransactionType())
                        .quantity(t.getQuantity())
                        .quantityBefore(t.getQuantityBefore())
                        .quantityAfter(t.getQuantityAfter())
                        .referenceId(t.getReferenceId())
                        .referenceType(t.getReferenceType())
                        .notes(t.getNotes())
                        .performedBy(t.getPerformedBy() != null ? t.getPerformedBy().getFullName() : null)
                        .transactionDate(t.getTransactionDate())
                        .build());
    }
}

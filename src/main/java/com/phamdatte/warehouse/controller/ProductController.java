package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.entity.Product;
import com.phamdatte.warehouse.entity.ProductCategory;
import com.phamdatte.warehouse.exception.BusinessException;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.ProductCategoryRepository;
import com.phamdatte.warehouse.repository.ProductRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<Page<Product>> getAll(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer categoryId,
            @PageableDefault(size = 20, sort = "productCode", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(productRepository.findByFilter(keyword, categoryId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Product> create(@RequestBody ProductRequest req) {
        if (req.getProductCode() != null && productRepository.existsByProductCode(req.getProductCode())) {
            throw new BusinessException("Product code already exists");
        }
        Product p = buildProduct(new Product(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(p));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Product> update(@PathVariable Integer id, @RequestBody ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        buildProduct(p, req);
        return ResponseEntity.ok(productRepository.save(p));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Product buildProduct(Product p, ProductRequest req) {
        p.setProductName(req.getProductName());
        p.setProductCode(req.getProductCode());
        p.setDescription(req.getDescription());
        p.setUnit(req.getUnit());
        p.setUnitPrice(req.getUnitPrice());
        p.setBarcode(req.getBarcode());
        if (req.getIsActive() != null) p.setIsActive(req.getIsActive());
        if (req.getCategoryId() != null) {
            ProductCategory cat = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + req.getCategoryId()));
            p.setCategory(cat);
        }
        return p;
    }

    // Simple DTO to accept flat fields from frontend
    public static class ProductRequest {
        private String productName;
        private String productCode;
        private String description;
        private String unit;
        private BigDecimal unitPrice;
        private String barcode;
        private Boolean isActive;
        private Integer categoryId;

        public String getProductName() { return productName; }
        public void setProductName(String v) { this.productName = v; }
        public String getProductCode() { return productCode; }
        public void setProductCode(String v) { this.productCode = v; }
        public String getDescription() { return description; }
        public void setDescription(String v) { this.description = v; }
        public String getUnit() { return unit; }
        public void setUnit(String v) { this.unit = v; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal v) { this.unitPrice = v; }
        public String getBarcode() { return barcode; }
        public void setBarcode(String v) { this.barcode = v; }
        public Boolean getIsActive() { return isActive; }
        public void setIsActive(Boolean v) { this.isActive = v; }
        public Integer getCategoryId() { return categoryId; }
        public void setCategoryId(Integer v) { this.categoryId = v; }
    }
}

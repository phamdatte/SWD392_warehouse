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
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Product> create(@Valid @RequestBody Product product) {
        if (productRepository.existsByProductCode(product.getProductCode())) {
            throw new BusinessException("Product code already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(productRepository.save(product));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Product> update(@PathVariable Integer id,
                                           @Valid @RequestBody Product req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        p.setProductName(req.getProductName());
        p.setDescription(req.getDescription());
        p.setUnit(req.getUnit());
        p.setUnitPrice(req.getUnitPrice());
        p.setBarcode(req.getBarcode());
        p.setIsActive(req.getIsActive());
        if (req.getCategory() != null && req.getCategory().getCategoryId() != null) {
            ProductCategory cat = categoryRepository.findById(req.getCategory().getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            p.setCategory(cat);
        }
        return ResponseEntity.ok(productRepository.save(p));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

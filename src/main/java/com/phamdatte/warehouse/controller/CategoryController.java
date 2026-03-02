package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.entity.ProductCategory;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.ProductCategoryRepository;
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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ProductCategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<Page<ProductCategory>> getAll(
            @PageableDefault(size = 100, sort = "categoryName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(categoryRepository.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductCategory> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<ProductCategory> create(@Valid @RequestBody ProductCategory category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryRepository.save(category));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<ProductCategory> update(@PathVariable Integer id,
                                                   @Valid @RequestBody ProductCategory req) {
        ProductCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        cat.setCategoryName(req.getCategoryName());
        cat.setDescription(req.getDescription());
        cat.setIsActive(req.getIsActive());
        return ResponseEntity.ok(categoryRepository.save(cat));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        categoryRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Void> toggleActive(@PathVariable Integer id) {
        ProductCategory cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + id));
        cat.setIsActive(!Boolean.TRUE.equals(cat.getIsActive()));
        categoryRepository.save(cat);
        return ResponseEntity.noContent().build();
    }
}

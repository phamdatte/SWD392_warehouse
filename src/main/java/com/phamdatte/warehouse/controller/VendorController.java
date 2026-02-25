package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.entity.Vendor;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.VendorRepository;
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
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorRepository vendorRepository;

    @GetMapping
    public ResponseEntity<Page<Vendor>> getAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "vendorName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(vendorRepository.findActiveByKeyword(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Vendor> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Vendor> create(@Valid @RequestBody Vendor vendor) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorRepository.save(vendor));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Vendor> update(@PathVariable Integer id,
                                          @Valid @RequestBody Vendor req) {
        Vendor v = vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id));
        v.setVendorName(req.getVendorName());
        v.setContactPerson(req.getContactPerson());
        v.setPhone(req.getPhone());
        v.setEmail(req.getEmail());
        v.setAddress(req.getAddress());
        v.setIsActive(req.getIsActive());
        return ResponseEntity.ok(vendorRepository.save(v));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vendorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found: " + id));
        vendorRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

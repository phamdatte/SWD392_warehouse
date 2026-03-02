package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.entity.Customer;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.CustomerRepository;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    public ResponseEntity<Page<Customer>> getAll(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "customerName", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(customerRepository.findActiveByKeyword(keyword, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Customer> create(@Valid @RequestBody Customer customer) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerRepository.save(customer));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Customer> update(@PathVariable Integer id,
                                            @Valid @RequestBody Customer req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        c.setCustomerName(req.getCustomerName());
        c.setContactPerson(req.getContactPerson());
        c.setPhone(req.getPhone());
        c.setEmail(req.getEmail());
        c.setAddress(req.getAddress());
        c.setIsActive(req.getIsActive());
        return ResponseEntity.ok(customerRepository.save(c));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        customerRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasAnyRole('Admin','Manager')")
    public ResponseEntity<Void> toggleActive(@PathVariable Integer id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
        c.setIsActive(!Boolean.TRUE.equals(c.getIsActive()));
        customerRepository.save(c);
        return ResponseEntity.noContent().build();
    }
}

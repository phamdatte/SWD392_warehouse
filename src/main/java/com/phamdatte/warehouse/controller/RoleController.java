package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.request.RolePagePermissionRequest;
import com.phamdatte.warehouse.dto.request.RoleRequest;
import com.phamdatte.warehouse.dto.response.RoleResponse;
import com.phamdatte.warehouse.dto.response.UserPageResponse;
import com.phamdatte.warehouse.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    // List all roles
    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    // Create a role
    @PostMapping
    public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(req));
    }

    // Update a role
    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable Integer id,
                                                   @Valid @RequestBody RoleRequest req) {
        return ResponseEntity.ok(roleService.updateRole(id, req));
    }

    // Toggle role active/inactive
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<RoleResponse> toggleActive(@PathVariable Integer id) {
        return ResponseEntity.ok(roleService.toggleActive(id));
    }

    // Get page permissions for a role
    @GetMapping("/{id}/pages")
    public ResponseEntity<List<UserPageResponse>> getRolePages(@PathVariable Integer id) {
        return ResponseEntity.ok(roleService.getRolePages(id));
    }

    // Save page permissions for a role
    @PutMapping("/{id}/pages")
    public ResponseEntity<Map<String, String>> saveRolePages(@PathVariable Integer id,
                                                             @RequestBody RolePagePermissionRequest req) {
        roleService.saveRolePages(id, req);
        return ResponseEntity.ok(Map.of("message", "Permissions saved successfully"));
    }
}

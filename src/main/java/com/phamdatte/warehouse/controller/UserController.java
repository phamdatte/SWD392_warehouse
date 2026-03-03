package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.request.CreateUserRequest;
import com.phamdatte.warehouse.dto.request.UpdateUserRequest;
import com.phamdatte.warehouse.dto.response.UserResponse;
import com.phamdatte.warehouse.service.UserService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('Admin')")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // UC21 - Danh sách user
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "username", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    // UC22 - Tạo user mới
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(req));
    }

    // Update user info (fullName, email, phone, optional password)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Integer id,
                                                   @Valid @RequestBody UpdateUserRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    // UC23 - Toggle active / deactivate user
    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<UserResponse> toggleActive(@PathVariable Integer id) {
        return ResponseEntity.ok(userService.toggleActive(id));
    }

    // Đổi role
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeRole(@PathVariable Integer id,
                                                    @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(userService.changeRole(id, body.get("roleId")));
    }
}


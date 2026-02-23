package com.phamdatte.warehouse.controller;

import com.phamdatte.warehouse.dto.request.LoginRequest;
import com.phamdatte.warehouse.dto.response.LoginResponse;
import com.phamdatte.warehouse.dto.response.UserPageResponse;
import com.phamdatte.warehouse.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // UC01 - Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // UC02 - Đăng xuất (stateless JWT — client xóa token phía frontend)
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    // Lấy danh sách page theo role (dùng cho sidebar)
    @GetMapping("/me/pages")
    public ResponseEntity<List<UserPageResponse>> getMyPages(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.getUserPages(userDetails.getUsername()));
    }
}

package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.request.CreateUserRequest;
import com.phamdatte.warehouse.dto.response.UserResponse;
import com.phamdatte.warehouse.entity.Role;
import com.phamdatte.warehouse.entity.User;
import com.phamdatte.warehouse.exception.BusinessException;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.RoleRepository;
import com.phamdatte.warehouse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    // UC21 - Danh sách user
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    // UC22 - Tạo user mới
    public UserResponse createUser(CreateUserRequest req) {
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new BusinessException("Username already exists");
        }
        Role role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = User.builder()
                .username(req.getUsername())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .fullName(req.getFullName())
                .email(req.getEmail())
                .phone(req.getPhone())
                .role(role)
                .isActive(true)
                .build();

        return toResponse(userRepository.save(user));
    }

    // UC23 - Kích hoạt / Vô hiệu hóa user
    public UserResponse toggleActive(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setIsActive(!user.getIsActive());
        return toResponse(userRepository.save(user));
    }

    // Đổi role
    public UserResponse changeRole(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        user.setRole(role);
        return toResponse(userRepository.save(user));
    }

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .userId(u.getUserId())
                .username(u.getUsername())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .roleId(u.getRole().getRoleId())
                .roleName(u.getRole().getRoleName())
                .isActive(u.getIsActive())
                .build();
    }
}

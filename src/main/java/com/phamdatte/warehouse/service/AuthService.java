package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.request.LoginRequest;
import com.phamdatte.warehouse.dto.response.LoginResponse;
import com.phamdatte.warehouse.dto.response.UserPageResponse;
import com.phamdatte.warehouse.entity.User;
import com.phamdatte.warehouse.repository.RolePageRepository;
import com.phamdatte.warehouse.repository.UserRepository;
import com.phamdatte.warehouse.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RolePageRepository rolePageRepository;

    public LoginResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            String token = jwtTokenProvider.generateToken(authentication.getName());

            User user = userRepository.findByUsername(request.getUsername()).orElseThrow();

            return LoginResponse.builder()
                    .token(token)
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getFullName())
                    .roleName(user.getRole().getRoleName())
                    .build();
        } catch (DisabledException e) {
            throw new com.phamdatte.warehouse.exception.BusinessException("This account has been locked. Please contact the administrator.");
        }
    }

    public List<UserPageResponse> getUserPages(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return rolePageRepository.findByRoleIdAndCanViewTrue(user.getRole().getRoleId())
                .stream()
                .map(rp -> UserPageResponse.builder()
                        .pageId(rp.getPage().getPageId())
                        .pageCode(rp.getPage().getPageCode())
                        .pageName(rp.getPage().getPageName())
                        .pageUrl(rp.getPage().getPageUrl())
                        .pageGroup(rp.getPage().getPageGroup())
                        .icon(rp.getPage().getIcon())
                        .displayOrder(rp.getPage().getDisplayOrder())
                        .isMenu(rp.getPage().getIsMenu())
                        .canView(rp.getCanView())
                        .canCreate(rp.getCanCreate())
                        .canEdit(rp.getCanEdit())
                        .canDelete(rp.getCanDelete())
                        .canApprove(rp.getCanApprove())
                        .build())
                .toList();
    }
}

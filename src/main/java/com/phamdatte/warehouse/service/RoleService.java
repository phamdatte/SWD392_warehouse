package com.phamdatte.warehouse.service;

import com.phamdatte.warehouse.dto.request.RolePagePermissionRequest;
import com.phamdatte.warehouse.dto.request.RoleRequest;
import com.phamdatte.warehouse.dto.response.RoleResponse;
import com.phamdatte.warehouse.dto.response.UserPageResponse;
import com.phamdatte.warehouse.entity.Page;
import com.phamdatte.warehouse.entity.Role;
import com.phamdatte.warehouse.entity.RolePage;
import com.phamdatte.warehouse.entity.RolePageId;
import com.phamdatte.warehouse.exception.BusinessException;
import com.phamdatte.warehouse.exception.ResourceNotFoundException;
import com.phamdatte.warehouse.repository.PageRepository;
import com.phamdatte.warehouse.repository.RolePageRepository;
import com.phamdatte.warehouse.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PageRepository pageRepository;
    private final RolePageRepository rolePageRepository;

    // List all roles
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Create a new role
    public RoleResponse createRole(RoleRequest req) {
        if (roleRepository.findByRoleName(req.getRoleName()).isPresent()) {
            throw new BusinessException("Role name already exists: " + req.getRoleName());
        }
        Role role = Role.builder()
                .roleName(req.getRoleName())
                .description(req.getDescription())
                .isActive(true)
                .build();
        return toResponse(roleRepository.save(role));
    }

    // Update an existing role
    public RoleResponse updateRole(Integer roleId, RoleRequest req) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        // Only update if name is changing to avoid unique conflict check self
        if (!role.getRoleName().equals(req.getRoleName())
                && roleRepository.findByRoleName(req.getRoleName()).isPresent()) {
            throw new BusinessException("Role name already exists: " + req.getRoleName());
        }
        role.setRoleName(req.getRoleName());
        role.setDescription(req.getDescription());
        return toResponse(roleRepository.save(role));
    }

    // Toggle role active/inactive
    public RoleResponse toggleActive(Integer roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        role.setIsActive(!Boolean.TRUE.equals(role.getIsActive()));
        return toResponse(roleRepository.save(role));
    }

    // Get all pages with permissions for a given role
    public List<UserPageResponse> getRolePages(Integer roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        // Load all active pages
        List<Page> allPages = pageRepository.findAllByIsActiveTrueOrderByDisplayOrder();

        // Load existing role_page entries and index by pageId
        List<RolePage> existing = rolePageRepository.findAllByRoleRoleId(roleId);
        Map<Integer, RolePage> rpMap = existing.stream()
                .collect(Collectors.toMap(rp -> rp.getPage().getPageId(), rp -> rp));

        return allPages.stream().map(page -> {
            RolePage rp = rpMap.get(page.getPageId());
            return UserPageResponse.builder()
                    .pageId(page.getPageId())
                    .pageCode(page.getPageCode())
                    .pageName(page.getPageName())
                    .pageUrl(page.getPageUrl())
                    .pageGroup(page.getPageGroup())
                    .icon(page.getIcon())
                    .displayOrder(page.getDisplayOrder())
                    .isMenu(page.getIsMenu())
                    .canView(rp != null && Boolean.TRUE.equals(rp.getCanView()))
                    .canCreate(rp != null && Boolean.TRUE.equals(rp.getCanCreate()))
                    .canEdit(rp != null && Boolean.TRUE.equals(rp.getCanEdit()))
                    .canDelete(rp != null && Boolean.TRUE.equals(rp.getCanDelete()))
                    .canApprove(rp != null && Boolean.TRUE.equals(rp.getCanApprove()))
                    .build();
        }).toList();
    }

    // Save role page permissions (bulk upsert)
    @Transactional
    public void saveRolePages(Integer roleId, RolePagePermissionRequest req) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));

        // Delete existing entries for this role
        rolePageRepository.deleteAllByRoleRoleId(roleId);

        // Insert new entries
        if (req.getPages() != null) {
            List<RolePage> toSave = req.getPages().stream()
                    .filter(item -> item.getPageId() != null)
                    .map(item -> {
                        Page page = pageRepository.findById(item.getPageId())
                                .orElseThrow(() -> new ResourceNotFoundException("Page not found: " + item.getPageId()));
                        return RolePage.builder()
                                .role(role)
                                .page(page)
                                .canView(Boolean.TRUE.equals(item.getCanView()))
                                .canCreate(Boolean.TRUE.equals(item.getCanCreate()))
                                .canEdit(Boolean.TRUE.equals(item.getCanEdit()))
                                .canDelete(Boolean.TRUE.equals(item.getCanDelete()))
                                .canApprove(Boolean.TRUE.equals(item.getCanApprove()))
                                .build();
                    })
                    .toList();
            rolePageRepository.saveAll(toSave);
        }
    }

    private RoleResponse toResponse(Role r) {
        return RoleResponse.builder()
                .roleId(r.getRoleId())
                .roleName(r.getRoleName())
                .description(r.getDescription())
                .isActive(r.getIsActive())
                .build();
    }
}

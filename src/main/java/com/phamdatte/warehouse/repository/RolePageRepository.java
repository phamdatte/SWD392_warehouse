package com.phamdatte.warehouse.repository;

import com.phamdatte.warehouse.entity.RolePage;
import com.phamdatte.warehouse.entity.RolePageId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RolePageRepository extends JpaRepository<RolePage, RolePageId> {

    @Query("SELECT rp FROM RolePage rp JOIN FETCH rp.page p WHERE rp.role.roleId = :roleId AND rp.canView = true AND p.isActive = true ORDER BY p.displayOrder")
    List<RolePage> findByRoleIdAndCanViewTrue(@Param("roleId") Integer roleId);

    List<RolePage> findAllByRoleRoleId(Integer roleId);

    void deleteAllByRoleRoleId(Integer roleId);
}


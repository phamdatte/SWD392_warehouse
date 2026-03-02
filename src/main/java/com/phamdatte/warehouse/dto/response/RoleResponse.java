package com.phamdatte.warehouse.dto.response;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class RoleResponse {
    private Integer roleId;
    private String roleName;
    private String description;
    private Boolean isActive;
}

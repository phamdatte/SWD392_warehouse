package com.phamdatte.warehouse.dto.response;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class UserResponse {
    private Integer userId;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Integer roleId;
    private String roleName;
    private Boolean isActive;
}

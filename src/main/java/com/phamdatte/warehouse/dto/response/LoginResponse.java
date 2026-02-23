package com.phamdatte.warehouse.dto.response;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class LoginResponse {
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private Integer userId;
    private String username;
    private String fullName;
    private String roleName;
}

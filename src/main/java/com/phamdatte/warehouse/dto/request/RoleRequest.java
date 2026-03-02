package com.phamdatte.warehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoleRequest {

    @NotBlank
    @Size(max = 50)
    private String roleName;

    @Size(max = 255)
    private String description;
}

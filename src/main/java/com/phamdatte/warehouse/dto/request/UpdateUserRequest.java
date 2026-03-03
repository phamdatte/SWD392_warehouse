package com.phamdatte.warehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequest {

    @NotBlank
    private String fullName;

    private String email;
    private String phone;

    // Optional: if blank/null, password is not changed
    private String password;
}

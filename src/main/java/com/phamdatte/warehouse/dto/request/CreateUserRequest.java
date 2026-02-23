package com.phamdatte.warehouse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CreateUserRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String fullName;

    private String email;
    private String phone;

    @NotNull
    private Integer roleId;
}

package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String realName,
        @NotBlank String role,
        String areaName
) {
}

package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateAreaRequest(
        @NotBlank String name,
        @NotBlank String code,
        Long parentId,
        String manager,
        @NotBlank String riskLevel,
        String address
) {
}

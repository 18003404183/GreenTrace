package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

public record StatusUpdateRequest(
        @NotBlank String status,
        String handler
) {
}

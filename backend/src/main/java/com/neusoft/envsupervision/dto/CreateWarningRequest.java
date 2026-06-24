package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWarningRequest(
        @NotBlank String title,
        @NotBlank String level,
        @NotBlank String areaName,
        @NotBlank String sourceType,
        String advice
) {
}

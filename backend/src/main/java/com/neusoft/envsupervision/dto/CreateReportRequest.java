package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateReportRequest(
        @NotBlank String reporterName,
        String phone,
        @NotBlank String areaName,
        @NotBlank String location,
        @NotBlank String category,
        @NotBlank String description,
        String source
) {
}

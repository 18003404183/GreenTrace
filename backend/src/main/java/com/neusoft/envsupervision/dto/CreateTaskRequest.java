package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotNull Long reportId,
        @NotBlank String title,
        @NotBlank String assignee,
        @NotBlank String deadline,
        @NotBlank String priority
) {
}

package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateWarningRuleRequest(
        @NotBlank String name,
        @NotBlank String metricName,
        @NotBlank String operator,
        @NotNull BigDecimal threshold,
        @NotBlank String level,
        Boolean enabled,
        String advice
) {
}

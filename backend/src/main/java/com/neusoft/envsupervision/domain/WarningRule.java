package com.neusoft.envsupervision.domain;

import java.math.BigDecimal;

public record WarningRule(
        Long id,
        String name,
        String metricName,
        String operator,
        BigDecimal threshold,
        String level,
        Boolean enabled,
        String advice
) {
}

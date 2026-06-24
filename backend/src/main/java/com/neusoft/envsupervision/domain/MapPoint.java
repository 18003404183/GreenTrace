package com.neusoft.envsupervision.domain;

import java.math.BigDecimal;

public record MapPoint(
        Long id,
        String name,
        String pointType,
        String areaName,
        String status,
        String riskLevel,
        BigDecimal xPercent,
        BigDecimal yPercent,
        String description
) {
}

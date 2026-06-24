package com.neusoft.envsupervision.domain;

import java.math.BigDecimal;

public record DeviceReading(
        Long id,
        Long deviceId,
        String metricName,
        BigDecimal metricValue,
        String unit,
        String recordedAt
) {
}

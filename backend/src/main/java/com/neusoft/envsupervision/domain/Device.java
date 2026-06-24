package com.neusoft.envsupervision.domain;

import java.math.BigDecimal;

public record Device(
        Long id,
        String name,
        String code,
        String type,
        String areaName,
        String status,
        String lastOnline,
        BigDecimal pm25,
        BigDecimal voc
) {
}

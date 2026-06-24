package com.neusoft.envsupervision.domain;

public record WarningItem(
        Long id,
        String title,
        String level,
        String areaName,
        String sourceType,
        String status,
        String createdAt,
        String advice
) {
}

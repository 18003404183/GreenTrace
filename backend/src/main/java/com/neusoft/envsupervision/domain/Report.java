package com.neusoft.envsupervision.domain;

public record Report(
        Long id,
        String reporterName,
        String phone,
        String areaName,
        String location,
        String category,
        String description,
        String status,
        String source,
        String createTime,
        String handler
) {
}

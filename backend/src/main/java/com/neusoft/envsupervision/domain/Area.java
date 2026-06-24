package com.neusoft.envsupervision.domain;

public record Area(
        Long id,
        String name,
        String code,
        Long parentId,
        String manager,
        String riskLevel,
        String address
) {
}

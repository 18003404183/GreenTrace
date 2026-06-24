package com.neusoft.envsupervision.domain;

public record OperationLog(
        Long id,
        String operator,
        String module,
        String action,
        String target,
        String detail,
        String createdAt
) {
}

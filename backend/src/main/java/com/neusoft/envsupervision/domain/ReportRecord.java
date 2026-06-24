package com.neusoft.envsupervision.domain;

public record ReportRecord(
        Long id,
        Long reportId,
        String action,
        String operator,
        String remark,
        String createdAt
) {
}

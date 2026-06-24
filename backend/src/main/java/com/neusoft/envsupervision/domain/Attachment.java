package com.neusoft.envsupervision.domain;

public record Attachment(
        Long id,
        Long reportId,
        Long recordId,
        String fileName,
        String filePath,
        String contentType,
        String usageType,
        String uploadedBy,
        String uploadedAt
) {
}

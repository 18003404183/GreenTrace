package com.neusoft.envsupervision.domain;

public record SupervisionTask(
        Long id,
        String taskNo,
        Long reportId,
        String title,
        String assignee,
        String deadline,
        String priority,
        String status,
        String createdAt,
        String completedAt
) {
}

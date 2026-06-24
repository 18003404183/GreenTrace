package com.neusoft.envsupervision.domain;

public record NotificationItem(
        Long id,
        String title,
        String content,
        String type,
        Boolean readFlag,
        String createdAt
) {
}

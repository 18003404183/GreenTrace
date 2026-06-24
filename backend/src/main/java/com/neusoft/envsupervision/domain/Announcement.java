package com.neusoft.envsupervision.domain;

public record Announcement(
        Long id,
        String title,
        String content,
        String publishedAt
) {
}

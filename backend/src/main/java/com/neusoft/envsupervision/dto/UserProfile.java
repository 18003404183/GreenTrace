package com.neusoft.envsupervision.dto;

public record UserProfile(
        Long id,
        String username,
        String realName,
        String role,
        String areaName,
        String status
) {
}

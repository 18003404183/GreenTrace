package com.neusoft.envsupervision.domain;

public record UserAccount(
        Long id,
        String username,
        String password,
        String realName,
        String role,
        String areaName,
        String status
) {
}

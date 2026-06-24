package com.neusoft.envsupervision.dto;

public record LoginResponse(
        String token,
        UserProfile user
) {
}

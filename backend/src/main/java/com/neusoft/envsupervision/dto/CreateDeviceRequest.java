package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record CreateDeviceRequest(
        @NotBlank String name,
        @NotBlank String code,
        @NotBlank String type,
        @NotBlank String areaName,
        @NotBlank String status,
        String lastOnline,
        BigDecimal pm25,
        BigDecimal voc
) {
}

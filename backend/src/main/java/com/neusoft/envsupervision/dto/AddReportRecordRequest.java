package com.neusoft.envsupervision.dto;

import jakarta.validation.constraints.NotBlank;

public record AddReportRecordRequest(
        @NotBlank String action,
        @NotBlank String operator,
        @NotBlank String remark
) {
}

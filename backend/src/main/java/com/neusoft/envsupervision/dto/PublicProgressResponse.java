package com.neusoft.envsupervision.dto;

import com.neusoft.envsupervision.domain.Report;
import com.neusoft.envsupervision.domain.ReportRecord;
import com.neusoft.envsupervision.domain.SupervisionTask;

import java.util.List;

public record PublicProgressResponse(
        Report report,
        List<ReportRecord> records,
        List<SupervisionTask> tasks
) {
}

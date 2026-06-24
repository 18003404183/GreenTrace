package com.neusoft.envsupervision.dto;

import com.neusoft.envsupervision.domain.Attachment;
import com.neusoft.envsupervision.domain.Report;
import com.neusoft.envsupervision.domain.ReportRecord;
import com.neusoft.envsupervision.domain.SupervisionTask;

import java.util.List;

public record ReportDetailResponse(
        Report report,
        List<ReportRecord> records,
        List<SupervisionTask> tasks,
        List<Attachment> attachments
) {
}

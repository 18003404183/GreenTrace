package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.Announcement;
import com.neusoft.envsupervision.domain.Report;
import com.neusoft.envsupervision.dto.CreateReportRequest;
import com.neusoft.envsupervision.dto.PublicProgressResponse;
import com.neusoft.envsupervision.mapper.AnnouncementMapper;
import com.neusoft.envsupervision.mapper.NotificationMapper;
import com.neusoft.envsupervision.mapper.ReportRecordMapper;
import com.neusoft.envsupervision.mapper.ReportMapper;
import com.neusoft.envsupervision.mapper.TaskMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/public")
public class PublicController {
    private final AnnouncementMapper announcementMapper;
    private final ReportMapper reportMapper;
    private final ReportRecordMapper reportRecordMapper;
    private final TaskMapper taskMapper;
    private final NotificationMapper notificationMapper;

    public PublicController(
            AnnouncementMapper announcementMapper,
            ReportMapper reportMapper,
            ReportRecordMapper reportRecordMapper,
            TaskMapper taskMapper,
            NotificationMapper notificationMapper
    ) {
        this.announcementMapper = announcementMapper;
        this.reportMapper = reportMapper;
        this.reportRecordMapper = reportRecordMapper;
        this.taskMapper = taskMapper;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping("/announcements")
    public ApiResponse<List<Announcement>> announcements() {
        return ApiResponse.ok(announcementMapper.findAll());
    }

    @PostMapping("/reports")
    public ApiResponse<Void> submitReport(@Valid @RequestBody CreateReportRequest request) {
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        reportMapper.insert(request, "公众端", createTime);
        Long reportId = reportMapper.findLatestId();
        reportRecordMapper.insert(reportId, "提交举报", request.reporterName(), "公众端提交环境问题线索，等待后台受理。", createTime);
        notificationMapper.insert(
                "\u65b0\u4e3e\u62a5\u5f85\u53d7\u7406",
                "\u516c\u4f17\u63d0\u4ea4\u4e86 " + request.areaName() + " " + request.category() + "\uff0c\u8bf7\u53ca\u65f6\u53d7\u7406\u5e76\u6d3e\u5355\u3002",
                "REPORT",
                createTime
        );
        return ApiResponse.ok(null);
    }

    @GetMapping("/progress")
    public ApiResponse<List<PublicProgressResponse>> progress(
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) String phone
    ) {
        List<Report> reports;
        if (reportId != null) {
            Report report = reportMapper.findById(reportId);
            reports = report == null ? List.of() : List.of(report);
        } else if (phone != null && !phone.isBlank()) {
            reports = reportMapper.findByPhone(phone);
        } else {
            return ApiResponse.fail("请输入举报编号或手机号");
        }
        return ApiResponse.ok(reports.stream()
                .map(report -> new PublicProgressResponse(
                        report,
                        reportRecordMapper.findByReportId(report.id()),
                        taskMapper.findByReportId(report.id())
                ))
                .toList());
    }
}

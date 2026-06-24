package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.Report;
import com.neusoft.envsupervision.dto.AddReportRecordRequest;
import com.neusoft.envsupervision.dto.CreateReportRequest;
import com.neusoft.envsupervision.dto.ReportDetailResponse;
import com.neusoft.envsupervision.dto.StatusUpdateRequest;
import com.neusoft.envsupervision.mapper.AttachmentMapper;
import com.neusoft.envsupervision.mapper.OperationLogMapper;
import com.neusoft.envsupervision.mapper.ReportRecordMapper;
import com.neusoft.envsupervision.mapper.ReportMapper;
import com.neusoft.envsupervision.mapper.TaskMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportMapper reportMapper;
    private final ReportRecordMapper reportRecordMapper;
    private final TaskMapper taskMapper;
    private final OperationLogMapper operationLogMapper;
    private final AttachmentMapper attachmentMapper;

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    public ReportController(
            ReportMapper reportMapper,
            ReportRecordMapper reportRecordMapper,
            TaskMapper taskMapper,
            OperationLogMapper operationLogMapper,
            AttachmentMapper attachmentMapper
    ) {
        this.reportMapper = reportMapper;
        this.reportRecordMapper = reportRecordMapper;
        this.taskMapper = taskMapper;
        this.operationLogMapper = operationLogMapper;
        this.attachmentMapper = attachmentMapper;
    }

    @GetMapping
    public ApiResponse<List<Report>> list() {
        return ApiResponse.ok(reportMapper.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<ReportDetailResponse> detail(@PathVariable Long id) {
        Report report = reportMapper.findById(id);
        if (report == null) {
            return ApiResponse.fail("举报线索不存在");
        }
        return ApiResponse.ok(new ReportDetailResponse(
                report,
                reportRecordMapper.findByReportId(id),
                taskMapper.findByReportId(id),
                attachmentMapper.findByReportId(id)
        ));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateReportRequest request) {
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        String source = request.source() == null || request.source().isBlank() ? "后台录入" : request.source();
        reportMapper.insert(request, source, createTime);
        Long reportId = reportMapper.findLatestId();
        reportRecordMapper.insert(reportId, "录入线索", request.reporterName(), "后台录入环境监督线索。", createTime);
        log(request.reporterName(), "监督数据", "录入线索", "举报 #" + reportId, request.category());
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        String handler = request.handler() == null || request.handler().isBlank() ? "系统管理员" : request.handler();
        reportMapper.updateStatus(id, request.status(), handler);
        String action = "已办结".equals(request.status()) ? "办结归档" : "受理线索";
        String remark = "已办结".equals(request.status()) ? "问题处置完成，进入归档闭环。" : "线索已受理，进入现场核查或派单流程。";
        reportRecordMapper.insert(id, action, handler, remark, now());
        log(handler, "监督数据", action, "举报 #" + id, request.status());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/records")
    public ApiResponse<Void> addRecord(@PathVariable Long id, @Valid @RequestBody AddReportRecordRequest request) {
        reportRecordMapper.insert(id, request.action(), request.operator(), request.remark(), now());
        log(request.operator(), "监督数据", request.action(), "举报 #" + id, request.remark());
        return ApiResponse.ok(null);
    }

    @PostMapping("/{id}/attachments")
    public ApiResponse<Void> uploadAttachment(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "举报证据") String usageType,
            @RequestParam(defaultValue = "系统管理员") String uploadedBy
    ) throws IOException {
        if (reportMapper.findById(id) == null) {
            return ApiResponse.fail("举报线索不存在");
        }
        if (file.isEmpty()) {
            return ApiResponse.fail("请选择要上传的附件");
        }
        String originalName = file.getOriginalFilename() == null ? "attachment" : file.getOriginalFilename();
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot).replaceAll("[^a-zA-Z0-9.]", "");
        }
        String storedName = UUID.randomUUID() + extension;
        Path targetDir = Path.of(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(targetDir);
        Path target = targetDir.resolve(storedName);
        file.transferTo(target);
        String filePath = "/uploads/" + storedName;
        String contentType = file.getContentType() == null ? "application/octet-stream" : file.getContentType();
        attachmentMapper.insert(id, null, originalName, filePath, contentType, usageType, uploadedBy, now());
        log(uploadedBy, "监督数据", "上传附件", "举报 #" + id, originalName);
        return ApiResponse.ok(null);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void log(String operator, String module, String action, String target, String detail) {
        operationLogMapper.insert(operator, module, action, target, detail, now());
    }
}

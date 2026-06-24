package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.domain.Report;
import com.neusoft.envsupervision.domain.SupervisionTask;
import com.neusoft.envsupervision.domain.WarningItem;
import com.neusoft.envsupervision.mapper.ReportMapper;
import com.neusoft.envsupervision.mapper.TaskMapper;
import com.neusoft.envsupervision.mapper.WarningMapper;
import com.neusoft.envsupervision.mapper.OperationLogMapper;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/exports")
public class ExportController {
    private final ReportMapper reportMapper;
    private final WarningMapper warningMapper;
    private final TaskMapper taskMapper;
    private final OperationLogMapper operationLogMapper;

    public ExportController(ReportMapper reportMapper, WarningMapper warningMapper, TaskMapper taskMapper, OperationLogMapper operationLogMapper) {
        this.reportMapper = reportMapper;
        this.warningMapper = warningMapper;
        this.taskMapper = taskMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping("/reports")
    public ResponseEntity<byte[]> reports() {
        StringBuilder csv = new StringBuilder("编号,举报人,电话,区域,地点,类型,状态,来源,创建时间,处理人\n");
        for (Report item : reportMapper.findAll()) {
            csv.append(row(item.id(), item.reporterName(), item.phone(), item.areaName(), item.location(), item.category(),
                    item.status(), item.source(), item.createTime(), item.handler()));
        }
        operationLogMapper.insert("系统管理员", "报表导出", "导出监督数据", "监督数据台账.csv", "导出举报线索台账。", now());
        return csv("监督数据台账.csv", csv);
    }

    @GetMapping("/warnings")
    public ResponseEntity<byte[]> warnings() {
        StringBuilder csv = new StringBuilder("编号,标题,等级,区域,来源,状态,创建时间,建议\n");
        for (WarningItem item : warningMapper.findAll()) {
            csv.append(row(item.id(), item.title(), item.level(), item.areaName(), item.sourceType(), item.status(),
                    item.createdAt(), item.advice()));
        }
        operationLogMapper.insert("系统管理员", "报表导出", "导出预警列表", "预警列表.csv", "导出监督预警台账。", now());
        return csv("预警列表.csv", csv);
    }

    @GetMapping("/tasks")
    public ResponseEntity<byte[]> tasks() {
        StringBuilder csv = new StringBuilder("编号,任务号,关联举报,标题,负责人,期限,优先级,状态,创建时间,完成时间\n");
        for (SupervisionTask item : taskMapper.findAll()) {
            csv.append(row(item.id(), item.taskNo(), item.reportId(), item.title(), item.assignee(), item.deadline(),
                    item.priority(), item.status(), item.createdAt(), item.completedAt()));
        }
        operationLogMapper.insert("系统管理员", "报表导出", "导出任务台账", "任务派单台账.csv", "导出任务派单台账。", now());
        return csv("任务派单台账.csv", csv);
    }

    private ResponseEntity<byte[]> csv(String filename, StringBuilder csv) {
        byte[] content = ("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8)
                .build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    private String row(Object... values) {
        StringBuilder row = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                row.append(',');
            }
            row.append(escape(values[i]));
        }
        return row.append('\n').toString();
    }

    private String escape(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"") + "\"";
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}

package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.SupervisionTask;
import com.neusoft.envsupervision.dto.CreateTaskRequest;
import com.neusoft.envsupervision.dto.StatusUpdateRequest;
import com.neusoft.envsupervision.mapper.OperationLogMapper;
import com.neusoft.envsupervision.mapper.NotificationMapper;
import com.neusoft.envsupervision.mapper.ReportMapper;
import com.neusoft.envsupervision.mapper.ReportRecordMapper;
import com.neusoft.envsupervision.mapper.TaskMapper;
import com.neusoft.envsupervision.mapper.WarningMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskMapper taskMapper;
    private final ReportMapper reportMapper;
    private final ReportRecordMapper reportRecordMapper;
    private final WarningMapper warningMapper;
    private final OperationLogMapper operationLogMapper;
    private final NotificationMapper notificationMapper;

    public TaskController(
            TaskMapper taskMapper,
            ReportMapper reportMapper,
            ReportRecordMapper reportRecordMapper,
            WarningMapper warningMapper,
            OperationLogMapper operationLogMapper,
            NotificationMapper notificationMapper
    ) {
        this.taskMapper = taskMapper;
        this.reportMapper = reportMapper;
        this.reportRecordMapper = reportRecordMapper;
        this.warningMapper = warningMapper;
        this.operationLogMapper = operationLogMapper;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ApiResponse<List<SupervisionTask>> list() {
        return ApiResponse.ok(taskMapper.findAll());
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateTaskRequest request) {
        if (reportMapper.findById(request.reportId()) == null) {
            return ApiResponse.fail("关联举报线索不存在");
        }
        String createdAt = now();
        String taskNo = "TASK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String deadline = request.deadline().replace('T', ' ');
        taskMapper.insert(taskNo, request.reportId(), request.title(), request.assignee(), deadline, request.priority(), createdAt);
        reportMapper.updateStatus(request.reportId(), "处理中", request.assignee());
        reportRecordMapper.insert(request.reportId(), "派单处理", request.assignee(), "已生成任务 " + taskNo + "，要求在 " + deadline + " 前完成。", createdAt);
        log(request.assignee(), "任务派单", "创建任务", taskNo, request.title());
        notificationMapper.insert(
                "新任务已派发",
                request.assignee() + " 需处理任务 " + taskNo + "：" + request.title(),
                "TASK",
                createdAt
        );
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        String completedAt = "已完成".equals(request.status()) ? now() : null;
        taskMapper.updateStatus(id, request.status(), completedAt);
        log(request.handler() == null || request.handler().isBlank() ? "系统管理员" : request.handler(),
                "任务派单", "更新任务状态", "任务 #" + id, request.status());
        return ApiResponse.ok(null);
    }

    @PostMapping("/check-overdue")
    public ApiResponse<Integer> checkOverdue() {
        String checkedAt = now();
        int generated = 0;
        for (SupervisionTask task : taskMapper.findOverdue(checkedAt)) {
            taskMapper.updateStatus(task.id(), "已逾期", null);
            String title = "任务逾期：" + task.title();
            if (warningMapper.countByTitle(title) == 0) {
                warningMapper.insertAuto(title, "高", "任务派单", "任务逾期", checkedAt,
                        "任务 " + task.taskNo() + " 已超过截止时间，建议督办负责人：" + task.assignee());
                notificationMapper.insert(
                        "任务逾期预警",
                        "任务 " + task.taskNo() + " 已逾期，请督办 " + task.assignee() + "。",
                        "WARNING",
                        checkedAt
                );
                generated++;
            }
            log("系统", "任务派单", "逾期检查", task.taskNo(), "任务已标记逾期并生成预警。");
        }
        return ApiResponse.ok(generated);
    }

    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private void log(String operator, String module, String action, String target, String detail) {
        operationLogMapper.insert(operator, module, action, target, detail, now());
    }
}

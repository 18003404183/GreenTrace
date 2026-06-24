package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.Device;
import com.neusoft.envsupervision.domain.DeviceReading;
import com.neusoft.envsupervision.domain.WarningItem;
import com.neusoft.envsupervision.domain.WarningRule;
import com.neusoft.envsupervision.dto.CreateWarningRequest;
import com.neusoft.envsupervision.dto.CreateWarningRuleRequest;
import com.neusoft.envsupervision.dto.StatusUpdateRequest;
import com.neusoft.envsupervision.mapper.DeviceMapper;
import com.neusoft.envsupervision.mapper.DeviceReadingMapper;
import com.neusoft.envsupervision.mapper.NotificationMapper;
import com.neusoft.envsupervision.mapper.OperationLogMapper;
import com.neusoft.envsupervision.mapper.WarningRuleMapper;
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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/warnings")
public class WarningController {
    private final WarningMapper warningMapper;
    private final WarningRuleMapper warningRuleMapper;
    private final DeviceMapper deviceMapper;
    private final DeviceReadingMapper deviceReadingMapper;
    private final OperationLogMapper operationLogMapper;
    private final NotificationMapper notificationMapper;

    public WarningController(
            WarningMapper warningMapper,
            WarningRuleMapper warningRuleMapper,
            DeviceMapper deviceMapper,
            DeviceReadingMapper deviceReadingMapper,
            OperationLogMapper operationLogMapper,
            NotificationMapper notificationMapper
    ) {
        this.warningMapper = warningMapper;
        this.warningRuleMapper = warningRuleMapper;
        this.deviceMapper = deviceMapper;
        this.deviceReadingMapper = deviceReadingMapper;
        this.operationLogMapper = operationLogMapper;
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ApiResponse<List<WarningItem>> list() {
        return ApiResponse.ok(warningMapper.findAll());
    }

    @GetMapping("/rules")
    public ApiResponse<List<WarningRule>> rules() {
        return ApiResponse.ok(warningRuleMapper.findAll());
    }

    @PostMapping("/rules")
    public ApiResponse<Void> createRule(@Valid @RequestBody CreateWarningRuleRequest request) {
        Boolean enabled = request.enabled() == null || request.enabled();
        warningRuleMapper.insert(request, enabled);
        log("系统管理员", "监督预警", "新增预警规则", request.name(), request.metricName() + request.operator() + request.threshold());
        return ApiResponse.ok(null);
    }

    @PatchMapping("/rules/{id}/enabled")
    public ApiResponse<Void> updateRuleEnabled(@PathVariable Long id, @RequestBody Map<String, Boolean> request) {
        warningRuleMapper.updateEnabled(id, request.getOrDefault("enabled", Boolean.TRUE));
        log("系统管理员", "监督预警", "切换规则状态", "规则 #" + id, String.valueOf(request.getOrDefault("enabled", Boolean.TRUE)));
        return ApiResponse.ok(null);
    }

    @PostMapping("/generate")
    public ApiResponse<Integer> generateByRules() {
        Map<Long, Device> devices = deviceMapper.findAll().stream().collect(Collectors.toMap(Device::id, Function.identity()));
        List<WarningRule> rules = warningRuleMapper.findEnabled();
        Map<String, DeviceReading> latestReadings = new LinkedHashMap<>();
        for (DeviceReading reading : deviceReadingMapper.findAll()) {
            String key = reading.deviceId() + "-" + reading.metricName();
            latestReadings.putIfAbsent(key, reading);
        }
        int generated = 0;
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        for (DeviceReading reading : latestReadings.values()) {
            Device device = devices.get(reading.deviceId());
            if (device == null) {
                continue;
            }
            for (WarningRule rule : rules) {
                if (rule.metricName().equals(reading.metricName()) && matched(reading, rule)) {
                    String title = device.areaName() + " " + rule.name() + "：" + reading.metricValue() + reading.unit();
                    if (warningMapper.countByTitle(title) == 0) {
                        warningMapper.insertAuto(title, rule.level(), device.areaName(), "规则引擎", now, rule.advice());
                        notificationMapper.insert(
                                "自动预警生成",
                                title + "，请进入监督预警模块处置。",
                                "WARNING",
                                now
                        );
                        generated++;
                    }
                    break;
                }
            }
        }
        log("系统", "监督预警", "规则生成预警", "规则引擎", "生成 " + generated + " 条预警。");
        return ApiResponse.ok(generated);
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateWarningRequest request) {
        String createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        warningMapper.insert(request, createdAt);
        log("系统管理员", "监督预警", "新增预警", request.title(), request.level());
        notificationMapper.insert(
                "人工预警新增",
                request.title() + "，风险等级：" + request.level(),
                "WARNING",
                createdAt
        );
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        warningMapper.updateStatus(id, request.status());
        String operator = request.handler() == null || request.handler().isBlank() ? "系统管理员" : request.handler();
        log(operator, "监督预警", "更新预警状态", "预警 #" + id, request.status());
        return ApiResponse.ok(null);
    }

    private boolean matched(DeviceReading reading, WarningRule rule) {
        int compared = reading.metricValue().compareTo(rule.threshold());
        return switch (rule.operator()) {
            case ">" -> compared > 0;
            case ">=" -> compared >= 0;
            case "<" -> compared < 0;
            case "<=" -> compared <= 0;
            case "=" -> compared == 0;
            default -> false;
        };
    }

    private void log(String operator, String module, String action, String target, String detail) {
        operationLogMapper.insert(operator, module, action, target, detail,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}

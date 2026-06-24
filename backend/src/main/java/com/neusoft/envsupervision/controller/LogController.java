package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.OperationLog;
import com.neusoft.envsupervision.mapper.OperationLogMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs")
public class LogController {
    private final OperationLogMapper operationLogMapper;

    public LogController(OperationLogMapper operationLogMapper) {
        this.operationLogMapper = operationLogMapper;
    }

    @GetMapping
    public ApiResponse<List<OperationLog>> list() {
        return ApiResponse.ok(operationLogMapper.findAll());
    }
}

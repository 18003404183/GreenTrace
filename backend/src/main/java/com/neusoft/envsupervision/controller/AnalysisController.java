package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.mapper.AnalysisMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisMapper analysisMapper;

    public AnalysisController(AnalysisMapper analysisMapper) {
        this.analysisMapper = analysisMapper;
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("activeUsers", analysisMapper.activeUsers());
        data.put("areas", analysisMapper.areas());
        data.put("devices", analysisMapper.devices());
        data.put("onlineDevices", analysisMapper.onlineDevices());
        data.put("reports", analysisMapper.reports());
        data.put("activeWarnings", analysisMapper.activeWarnings());
        data.put("categoryStats", analysisMapper.reportCategoryStats());
        data.put("statusStats", analysisMapper.reportStatusStats());
        data.put("areaStats", analysisMapper.areaReportStats());
        return ApiResponse.ok(data);
    }
}

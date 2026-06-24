package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.Area;
import com.neusoft.envsupervision.dto.CreateAreaRequest;
import com.neusoft.envsupervision.mapper.AreaMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/areas")
public class AreaController {
    private final AreaMapper areaMapper;

    public AreaController(AreaMapper areaMapper) {
        this.areaMapper = areaMapper;
    }

    @GetMapping
    public ApiResponse<List<Area>> list() {
        return ApiResponse.ok(areaMapper.findAll());
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateAreaRequest request) {
        areaMapper.insert(request);
        return ApiResponse.ok(null);
    }
}

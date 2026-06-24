package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.MapPoint;
import com.neusoft.envsupervision.mapper.MapPointMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/map-points")
public class MapController {
    private final MapPointMapper mapPointMapper;

    public MapController(MapPointMapper mapPointMapper) {
        this.mapPointMapper = mapPointMapper;
    }

    @GetMapping
    public ApiResponse<List<MapPoint>> list() {
        return ApiResponse.ok(mapPointMapper.findAll());
    }
}

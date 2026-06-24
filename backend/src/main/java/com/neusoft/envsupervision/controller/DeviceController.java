package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.Device;
import com.neusoft.envsupervision.domain.DeviceReading;
import com.neusoft.envsupervision.dto.CreateDeviceRequest;
import com.neusoft.envsupervision.dto.StatusUpdateRequest;
import com.neusoft.envsupervision.mapper.DeviceReadingMapper;
import com.neusoft.envsupervision.mapper.DeviceMapper;
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
@RequestMapping("/api/devices")
public class DeviceController {
    private final DeviceMapper deviceMapper;
    private final DeviceReadingMapper deviceReadingMapper;

    public DeviceController(DeviceMapper deviceMapper, DeviceReadingMapper deviceReadingMapper) {
        this.deviceMapper = deviceMapper;
        this.deviceReadingMapper = deviceReadingMapper;
    }

    @GetMapping
    public ApiResponse<List<Device>> list() {
        return ApiResponse.ok(deviceMapper.findAll());
    }

    @GetMapping("/{id}/readings")
    public ApiResponse<List<DeviceReading>> readings(@PathVariable Long id) {
        return ApiResponse.ok(deviceReadingMapper.findByDeviceId(id));
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateDeviceRequest request) {
        deviceMapper.insert(request);
        return ApiResponse.ok(null);
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        deviceMapper.updateStatus(id, request.status(), now);
        return ApiResponse.ok(null);
    }
}

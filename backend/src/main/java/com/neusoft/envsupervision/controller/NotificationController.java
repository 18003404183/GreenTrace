package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.NotificationItem;
import com.neusoft.envsupervision.mapper.NotificationMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationMapper notificationMapper;

    public NotificationController(NotificationMapper notificationMapper) {
        this.notificationMapper = notificationMapper;
    }

    @GetMapping
    public ApiResponse<List<NotificationItem>> list() {
        return ApiResponse.ok(notificationMapper.findAll());
    }

    @PatchMapping("/{id}/read")
    public ApiResponse<Void> markRead(@PathVariable Long id) {
        notificationMapper.markRead(id);
        return ApiResponse.ok(null);
    }
}

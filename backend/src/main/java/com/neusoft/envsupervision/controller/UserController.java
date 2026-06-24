package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.UserAccount;
import com.neusoft.envsupervision.dto.CreateUserRequest;
import com.neusoft.envsupervision.dto.UserProfile;
import com.neusoft.envsupervision.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public ApiResponse<List<UserProfile>> list() {
        return ApiResponse.ok(userMapper.findAll().stream().map(this::toProfile).toList());
    }

    @PostMapping
    public ApiResponse<Void> create(@Valid @RequestBody CreateUserRequest request) {
        userMapper.insert(request);
        return ApiResponse.ok(null);
    }

    private UserProfile toProfile(UserAccount user) {
        return new UserProfile(user.id(), user.username(), user.realName(), user.role(), user.areaName(), user.status());
    }
}

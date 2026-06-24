package com.neusoft.envsupervision.controller;

import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.UserAccount;
import com.neusoft.envsupervision.dto.LoginRequest;
import com.neusoft.envsupervision.dto.LoginResponse;
import com.neusoft.envsupervision.dto.UserProfile;
import com.neusoft.envsupervision.mapper.OperationLogMapper;
import com.neusoft.envsupervision.mapper.UserMapper;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserMapper userMapper;
    private final OperationLogMapper operationLogMapper;

    public AuthController(UserMapper userMapper, OperationLogMapper operationLogMapper) {
        this.userMapper = userMapper;
        this.operationLogMapper = operationLogMapper;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserAccount user = userMapper.findByUsername(request.username());
        if (user == null || !user.password().equals(request.password()) || !"启用".equals(user.status())) {
            return ApiResponse.fail("用户名或密码错误");
        }
        String rawToken = user.username() + ":" + LocalDateTime.now();
        String token = Base64.getEncoder().encodeToString(rawToken.getBytes(StandardCharsets.UTF_8));
        UserProfile profile = toProfile(user);
        operationLogMapper.insert(user.realName(), "用户权限", "登录系统", user.username(), "用户登录环保公众监督系统。", now());
        return ApiResponse.ok(new LoginResponse(token, profile));
    }

    private UserProfile toProfile(UserAccount user) {
        return new UserProfile(user.id(), user.username(), user.realName(), user.role(), user.areaName(), user.status());
    }

    private String now() {
        return LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}

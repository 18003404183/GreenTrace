package com.neusoft.envsupervision.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neusoft.envsupervision.common.ApiResponse;
import com.neusoft.envsupervision.domain.UserAccount;
import com.neusoft.envsupervision.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Set;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String ENABLED_STATUS = "\u542f\u7528";

    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(UserMapper userMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (isPublicPath(path)) {
            return true;
        }

        UserAccount user = currentUser(request);
        if (user == null || !ENABLED_STATUS.equals(user.status())) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "\u8bf7\u5148\u767b\u5f55\u7cfb\u7edf");
            return false;
        }

        if (!allowed(path, request.getMethod(), user.role())) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "\u5f53\u524d\u89d2\u8272\u65e0\u6743\u8bbf\u95ee\u8be5\u529f\u80fd");
            return false;
        }

        request.setAttribute("currentUser", user);
        request.setAttribute("currentRole", user.role());
        request.setAttribute("currentRealName", user.realName());
        return true;
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/")
                || path.startsWith("/api/public/")
                || path.startsWith("/uploads/")
                || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console/");
    }

    private UserAccount currentUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authorization.substring("Bearer ".length());
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            int separator = decoded.indexOf(':');
            if (separator <= 0) {
                return null;
            }
            return userMapper.findByUsername(decoded.substring(0, separator));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean allowed(String path, String method, String role) {
        if ("ADMIN".equals(role)) {
            return true;
        }
        boolean read = "GET".equalsIgnoreCase(method);
        if (path.startsWith("/api/users") || path.startsWith("/api/logs")) {
            return false;
        }
        if (path.startsWith("/api/areas")) {
            return read;
        }
        if (path.startsWith("/api/devices")) {
            return read && hasRole(role, "ANALYST");
        }
        if (path.startsWith("/api/reports") || path.startsWith("/api/tasks")) {
            return hasRole(role, "INSPECTOR");
        }
        if (path.startsWith("/api/warnings/rules")) {
            return read;
        }
        if (path.startsWith("/api/warnings/generate") || path.startsWith("/api/warnings/")) {
            return read || hasRole(role, "INSPECTOR");
        }
        if (path.equals("/api/warnings")) {
            return read || hasRole(role, "INSPECTOR");
        }
        if (path.startsWith("/api/analysis")
                || path.startsWith("/api/map-points")
                || path.startsWith("/api/notifications")
                || path.startsWith("/api/exports")) {
            return hasRole(role, "INSPECTOR", "ANALYST");
        }
        return hasRole(role, "INSPECTOR", "ANALYST");
    }

    private boolean hasRole(String role, String... roles) {
        return Set.of(roles).contains(role);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws Exception {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(message));
    }
}

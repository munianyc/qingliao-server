package com.qingliao.server.controller;

import com.qingliao.server.dto.*;
import com.qingliao.server.security.SecurityUtil;
import com.qingliao.server.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    public AuthController(AuthService authService, SecurityUtil securityUtil) {
        this.authService = authService;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody RegisterRequest req) {
        String error = authService.register(req);
        if (error != null) return ApiResponse.error(400, error);
        return ApiResponse.ok();
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse resp = authService.login(req);
        if (resp == null) return ApiResponse.error(401, "用户名或密码错误");
        return ApiResponse.ok(resp);
    }

    @PostMapping("/logout")
    public ApiResponse<?> logout() {
        Long userId = securityUtil.getCurrentUserId();
        if (userId != null) authService.logout(userId);
        return ApiResponse.ok();
    }
}

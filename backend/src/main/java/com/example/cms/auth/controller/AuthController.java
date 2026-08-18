package com.example.cms.auth.controller;

import com.example.cms.auth.dto.LoginRequest;
import com.example.cms.auth.dto.RefreshTokenRequest;
import com.example.cms.auth.service.AuthService;
import com.example.cms.auth.vo.LoginVO;
import com.example.cms.auth.vo.UserInfoVO;
import com.example.cms.common.response.ApiResponse;
import com.example.cms.common.utils.SecurityUtils;
import com.example.cms.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "登录、登出、Token刷新、用户信息")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.login(request, httpRequest));
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public ApiResponse<LoginVO> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/userinfo")
    public ApiResponse<UserInfoVO> userInfo() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ApiResponse.success(authService.getCurrentUserInfo(userId));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.success();
    }
}

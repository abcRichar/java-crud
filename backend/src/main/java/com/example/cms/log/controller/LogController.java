package com.example.cms.log.controller;

import com.example.cms.common.response.ApiResponse;
import com.example.cms.common.response.PageResult;
import com.example.cms.log.service.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "日志管理", description = "登录日志、操作日志")
@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @Operation(summary = "登录日志列表")
    @GetMapping("/login")
    @PreAuthorize("hasAuthority('log:login:list')")
    public ApiResponse<PageResult<Map<String, Object>>> loginLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(logService.getLoginLogs(keyword, status, page, pageSize));
    }

    @Operation(summary = "操作日志列表")
    @GetMapping("/operation")
    @PreAuthorize("hasAuthority('log:operation:list')")
    public ApiResponse<PageResult<Map<String, Object>>> operationLogs(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        return ApiResponse.success(logService.getOperationLogs(keyword, page, pageSize));
    }
}

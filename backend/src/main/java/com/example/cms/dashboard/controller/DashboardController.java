package com.example.cms.dashboard.controller;

import com.example.cms.common.response.ApiResponse;
import com.example.cms.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Dashboard", description = "仪表盘统计数据")
@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "汇总统计")
    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @Operation(summary = "用户增长趋势")
    @GetMapping("/user-trend")
    public ApiResponse<Map<String, Object>> userTrend(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(dashboardService.getUserGrowthTrend(days));
    }

    @Operation(summary = "文章发布趋势")
    @GetMapping("/article-trend")
    public ApiResponse<Map<String, Object>> articleTrend(@RequestParam(defaultValue = "30") int days) {
        return ApiResponse.success(dashboardService.getArticleTrend(days));
    }

    @Operation(summary = "分类文章统计")
    @GetMapping("/category-stats")
    public ApiResponse<Map<String, Object>> categoryStats() {
        return ApiResponse.success(dashboardService.getCategoryStats());
    }
}

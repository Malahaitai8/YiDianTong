package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理端统计报表", description = "提供全局统计数据报表")
@RestController
@RequestMapping("/api/admin/stats")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class AdminStatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @Operation(summary = "获取全局概览统计", description = "提供全站的核心运营指标，如预约量、完成率、号源利用率等")
    @GetMapping("/overview")
    public Result getOverviewStats() {
        try {
            return Result.success(statisticsService.getOverviewStats());
        } catch (Exception e) {
            return Result.error("获取统计数据失败: " + e.getMessage());
        }
    }

}


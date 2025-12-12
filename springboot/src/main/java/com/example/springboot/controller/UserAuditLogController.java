package com.example.springboot.controller;

import com.example.springboot.annotation.AuditLog;
import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.AuditLogQueryRequest;
import com.example.springboot.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供患者/医生查看自身审计日志的接口（与管理员接口隔离）
 */
@Tag(name = "审计日志（自助）", description = "患者/医生查看自己的审计日志")
@RestController
@RequestMapping("/api/audit-logs")
@SecurityRequirement(name = "bearer-jwt")
public class UserAuditLogController {

    @Resource
    private AuditLogService auditLogService;

    @Operation(summary = "查询我的审计日志", description = "仅返回当前登录用户的操作日志，支持分页/筛选")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR','ADMIN')")
    @AuditLog(operationType = "QUERY", operationModule = "AUDIT", operationDesc = "查询个人审计日志", recordResponse = false)
    public Result myLogs(AuditLogQueryRequest request) {
        // 强制只查当前用户，避免越权
        Long userId = SecurityUtils.getCurrentUserId();
        request.setUserId(userId);
        request.setUsername(SecurityUtils.getCurrentUsername());
        return Result.success(auditLogService.queryLogs(request));
    }
}


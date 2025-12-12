package com.example.springboot.controller;

import com.example.springboot.annotation.AuditLog;
import com.example.springboot.common.Result;
import com.example.springboot.dto.AuditLogQueryRequest;
import com.example.springboot.service.AuditLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "审计日志", description = "审计日志查询接口")
@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class AuditLogController {

    @Resource
    private AuditLogService auditLogService;

    @Operation(summary = "查询审计日志列表", description = "支持多条件筛选和分页")
    @GetMapping
    @AuditLog(operationType = "QUERY", operationModule = "AUDIT", operationDesc = "查询审计日志列表", recordResponse = false)
    public Result queryLogs(AuditLogQueryRequest request) {
        return Result.success(auditLogService.queryLogs(request));
    }

    @Operation(summary = "查询审计日志详情", description = "根据日志ID查询详细信息")
    @GetMapping("/{id}")
    @AuditLog(operationType = "QUERY", operationModule = "AUDIT", operationDesc = "查看审计日志详情", recordResponse = false)
    public Result getLogById(@PathVariable Long id) {
        return Result.success(auditLogService.getById(id));
    }
}





package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.*;
import com.example.springboot.service.ScheduleRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 排班规则管理控制器
 */
@Tag(name = "排班规则管理", description = "管理员制定和管理医生排班规则的接口")
@RestController
@RequestMapping("/api/admin/schedule-rules")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class ScheduleRuleController {

    @Resource
    private ScheduleRuleService scheduleRuleService;

    /**
     * 创建排班规则
     */
    @Operation(summary = "创建排班规则", description = "管理员创建新的排班规则模板")
    @PostMapping
    public Result createRule(@Valid @RequestBody CreateScheduleRuleRequest request) {
        try {
            String username = SecurityUtils.getCurrentUsername();
            ScheduleRuleDetailDTO rule = scheduleRuleService.createRule(request, username);
            return Result.success(rule);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有排班规则
     */
    @Operation(summary = "查询所有排班规则", description = "获取系统中所有的排班规则列表")
    @GetMapping
    public Result getAllRules() {
        try {
            List<ScheduleRuleDetailDTO> rules = scheduleRuleService.getAllRules();
            return Result.success(rules);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询规则
     */
    @Operation(summary = "查询规则详情", description = "根据规则ID查询单个规则的详细信息")
    @GetMapping("/{ruleId}")
    public Result getRuleById(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId) {
        try {
            ScheduleRuleDetailDTO rule = scheduleRuleService.getRuleById(ruleId);
            return Result.success(rule);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据医生ID查询规则
     */
    @Operation(summary = "查询医生的排班规则", description = "获取指定医生的所有排班规则")
    @GetMapping("/doctor/{doctorId}")
    public Result getRulesByDoctorId(
            @Parameter(description = "医生ID", required = true) @PathVariable Long doctorId) {
        try {
            List<ScheduleRuleDetailDTO> rules = scheduleRuleService.getRulesByDoctorId(doctorId);
            return Result.success(rules);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态查询规则
     */
    @Operation(summary = "按状态查询规则", description = "查询指定状态的排班规则（ACTIVE/INACTIVE/EXPIRED）")
    @GetMapping("/status/{status}")
    public Result getRulesByStatus(
            @Parameter(description = "状态：ACTIVE、INACTIVE 或 EXPIRED", required = true) @PathVariable String status) {
        try {
            List<ScheduleRuleDetailDTO> rules = scheduleRuleService.getRulesByStatus(status);
            return Result.success(rules);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 更新排班规则
     */
    @Operation(summary = "更新排班规则", description = "修改现有排班规则的信息")
    @PutMapping("/{ruleId}")
    public Result updateRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId,
            @Valid @RequestBody UpdateScheduleRuleRequest request) {
        try {
            ScheduleRuleDetailDTO rule = scheduleRuleService.updateRule(ruleId, request);
            return Result.success(rule);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除排班规则
     */
    @Operation(summary = "删除排班规则", description = "删除指定的排班规则（谨慎操作）")
    @DeleteMapping("/{ruleId}")
    public Result deleteRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId) {
        try {
            scheduleRuleService.deleteRule(ruleId);
            return Result.success("规则删除成功");
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 启用规则
     */
    @Operation(summary = "启用排班规则", description = "将规则状态设置为 ACTIVE")
    @PostMapping("/{ruleId}/enable")
    public Result enableRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId) {
        try {
            scheduleRuleService.enableRule(ruleId);
            return Result.success("规则已启用");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 禁用规则
     */
    @Operation(summary = "禁用排班规则", description = "将规则状态设置为 INACTIVE")
    @PostMapping("/{ruleId}/disable")
    public Result disableRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId) {
        try {
            scheduleRuleService.disableRule(ruleId);
            return Result.success("规则已禁用");
        } catch (Exception e) {
            return Result.error("操作失败: " + e.getMessage());
        }
    }

    /**
     * 应用规则生成排班
     */
    @Operation(summary = "应用规则生成排班", 
               description = "根据规则自动批量创建排班记录，支持覆盖已存在的排班")
    @PostMapping("/{ruleId}/apply")
    public Result applyRule(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId,
            @RequestBody(required = false) ApplyScheduleRuleRequest request) {
        try {
            // 如果没有传入请求体，创建默认的
            if (request == null) {
                request = new ApplyScheduleRuleRequest();
            }
            request.setRuleId(String.valueOf(ruleId));
            
            Map<String, Object> result = scheduleRuleService.applyRule(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("应用失败: " + e.getMessage());
        }
    }

    /**
     * 检测规则冲突
     */
    @Operation(summary = "检测规则冲突", 
               description = "检查指定规则与其他规则是否存在冲突（同一医生、重叠时间）")
    @GetMapping("/{ruleId}/conflicts")
    public Result detectConflicts(
            @Parameter(description = "规则ID", required = true) @PathVariable Long ruleId) {
        try {
            Map<String, Object> result = scheduleRuleService.detectConflicts(ruleId);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("检测失败: " + e.getMessage());
        }
    }
}


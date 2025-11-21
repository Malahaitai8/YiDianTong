package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.*;
import com.example.springboot.entity.Schedule;
import com.example.springboot.service.ScheduleService;
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
 * 管理端排班管理控制器
 */
@Tag(name = "管理端排班管理", description = "管理员排班管理相关接口")
@RestController
@RequestMapping("/api/admin/schedules")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class AdminScheduleController {

    @Resource
    private ScheduleService scheduleService;

    /**
     * 创建单个排班
     * POST /api/admin/schedules
     */
    @Operation(summary = "创建单个排班", description = "为指定医生创建单个排班记录")
    @PostMapping
    public Result createSchedule(@Valid @RequestBody CreateScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.createSchedule(request);
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error("创建失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建排班
     * POST /api/admin/schedules/batch
     */
    @Operation(summary = "批量创建排班", description = "为指定医生批量创建多个排班记录")
    @PostMapping("/batch")
    public Result batchCreateSchedule(@Valid @RequestBody BatchScheduleRequest request) {
        try {
            Map<String, Object> result = scheduleService.batchCreateSchedule(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("批量创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新排班
     * PUT /api/admin/schedules/{id}
     */
    @Operation(summary = "更新排班", description = "更新指定排班的信息")
    @PutMapping("/{id}")
    public Result updateSchedule(
            @Parameter(description = "排班ID", required = true) @PathVariable Long id,
            @Valid @RequestBody UpdateScheduleRequest request) {
        try {
            Schedule schedule = scheduleService.updateSchedule(id, request);
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除排班
     * DELETE /api/admin/schedules/{id}
     */
    @Operation(summary = "删除排班", description = "删除指定的排班记录")
    @DeleteMapping("/{id}")
    public Result deleteSchedule(@Parameter(description = "排班ID", required = true) @PathVariable Long id) {
        try {
            scheduleService.deleteSchedule(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error("删除失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询排班详情
     * GET /api/admin/schedules/{id}
     */
    @Operation(summary = "查询排班详情", description = "根据ID查询单个排班的详细信息")
    @GetMapping("/{id}")
    public Result getScheduleById(@Parameter(description = "排班ID", required = true) @PathVariable Long id) {
        try {
            Schedule schedule = scheduleService.getScheduleById(id);
            return Result.success(schedule);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 条件查询排班列表（带分页和筛选）
     * GET /api/admin/schedules
     * 
     * 查询参数：
     * - doctorId: 医生ID（可选）
     * - departmentId: 科室ID（可选）
     * - startDate: 开始日期（可选，格式：yyyy-MM-dd）
     * - endDate: 结束日期（可选，格式：yyyy-MM-dd）
     * - timeSlot: 时间段（可选，如：morning/afternoon/evening）
     * - slotType: 号别（可选，如：normal/expert/vip）
     * - page: 页码（默认1）
     * - pageSize: 每页大小（默认20）
     */
    @Operation(summary = "条件查询排班列表", description = "根据多种条件筛选和查询排班列表，支持分页")
    @GetMapping
    public Result querySchedules(ScheduleQueryRequest request) {
        try {
            Map<String, Object> result = scheduleService.querySchedules(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据医生ID查询排班列表
     * GET /api/admin/schedules/doctor/{doctorId}
     */
    @Operation(summary = "查询医生的所有排班", description = "查询指定医生的所有排班记录（包含医生姓名、门诊信息等详细信息）")
    @GetMapping("/doctor/{doctorId}")
    public Result getSchedulesByDoctorId(@Parameter(description = "医生ID", required = true) @PathVariable Long doctorId) {
        try {
            List<ScheduleWithDetailsDTO> schedules = scheduleService.getSchedulesByDoctorIdWithDetails(doctorId);
            Map<String, Object> data = Map.of(
                "data", schedules,
                "total", schedules.size()
            );
            return Result.success(data);
        } catch (Exception e) {
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 加号（增加号源数量）
     * POST /api/admin/schedules/{id}/add-slots
     */
    @Operation(summary = "加号并处理候补", description = "为指定排班增加号源，并自动处理候补队列")
    @PostMapping("/{id}/add-slots")
    public Result addSlots(
            @Parameter(description = "排班ID", required = true) @PathVariable Long id,
            @RequestBody @Valid AddSlotsRequest request) {
        try {
            Map<String, Object> result = scheduleService.addSlots(id, request.getSlotsToAdd());
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("加号失败: " + e.getMessage());
        }
    }
}


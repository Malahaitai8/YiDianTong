package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.ApproveScheduleChangeRequest;
import com.example.springboot.dto.ScheduleChangeRequestDTO;
import com.example.springboot.dto.ScheduleChangeRequestItem;
import com.example.springboot.service.ScheduleChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 调班申请控制器
 * 医生提交调班申请，管理员审核
 */
@Tag(name = "调班申请管理", description = "医生调班申请提交与管理员审核接口（使用system_config JSON存储）")
@RestController
@RequestMapping("/api/schedule-change")
@SecurityRequirement(name = "bearer-jwt")
public class ScheduleChangeRequestController {

    @Resource
    private ScheduleChangeRequestService service;

    @Operation(summary = "医生提交调班申请", description = "医生提交修改排班的申请，需要管理员审核后生效")
    @PostMapping("/submit")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result submit(@RequestBody ScheduleChangeRequestDTO dto) {
        dto.setRequestedByUserId(SecurityUtils.getCurrentUserId());
        ScheduleChangeRequestItem item = service.submit(dto);
        return Result.success(item);
    }

    @Operation(summary = "医生查看本人调班申请列表", description = "医生查询自己提交的所有调班申请")
    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result myList() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<ScheduleChangeRequestItem> list = service.listByCurrentDoctor(userId);
        return Result.success(list);
    }

    @Operation(summary = "按医生ID查询调班申请列表", description = "查询指定医生的所有调班申请")
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result listByDoctor(@PathVariable Long doctorId) {
        List<ScheduleChangeRequestItem> list = service.listByDoctor(doctorId);
        return Result.success(list);
    }

    @Operation(summary = "管理员查询所有调班申请", description = "管理员查询所有调班申请，可按状态过滤（PENDING/APPROVED/REJECTED）")
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result adminList(@RequestParam(required = false) String status) {
        List<ScheduleChangeRequestItem> list = service.listAll(Optional.ofNullable(status));
        return Result.success(list);
    }

    @Operation(summary = "管理员审核调班申请", description = "管理员审核调班申请，通过后自动修改排班信息，拒绝则保持原样")
    @PostMapping("/admin/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result review(@RequestBody ApproveScheduleChangeRequest req) {
        String adminUsername = SecurityUtils.getCurrentUsername();
        ScheduleChangeRequestItem item = service.review(req, adminUsername);
        return Result.success(item);
    }
}

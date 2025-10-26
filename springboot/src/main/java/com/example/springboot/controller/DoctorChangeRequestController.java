package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.ApproveDoctorChangeRequest;
import com.example.springboot.dto.DoctorChangeRequestDTO;
import com.example.springboot.dto.DoctorChangeRequestItem;
import com.example.springboot.service.DoctorChangeRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "医生信息变更申请", description = "医生变更提交与管理员审核接口（使用system_config JSON存储）")
@RestController
@RequestMapping("/api/doctor-change")
@SecurityRequirement(name = "bearer-jwt")
public class DoctorChangeRequestController {

    @Resource
    private DoctorChangeRequestService service;

    @Operation(summary = "医生提交变更申请")
    @PostMapping("/submit")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result submit(@RequestBody DoctorChangeRequestDTO dto) {
        dto.setRequestedByUserId(SecurityUtils.getCurrentUserId());
        DoctorChangeRequestItem item = service.submit(dto);
        return Result.success(item);
    }

    @Operation(summary = "医生查看本人变更申请列表")
    @GetMapping("/my")
    @PreAuthorize("hasRole('DOCTOR')")
    public Result myList() {
        Long userId = SecurityUtils.getCurrentUserId();
        // 通过 userId 反查 doctorId
        List<DoctorChangeRequestItem> list = service.listByCurrentDoctor(userId);
        return Result.success(list);
    }

    @Operation(summary = "按医生ID查询变更申请列表")
    @GetMapping("/by-doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public Result listByDoctor(@PathVariable Long doctorId) {
        List<DoctorChangeRequestItem> list = service.listByDoctor(doctorId);
        return Result.success(list);
    }

    @Operation(summary = "管理员查询变更申请（可按状态过滤）")
    @GetMapping("/admin/list")
    @PreAuthorize("hasRole('ADMIN')")
    public Result adminList(@RequestParam(required = false) String status) {
        List<DoctorChangeRequestItem> list = service.listAll(Optional.ofNullable(status));
        return Result.success(list);
    }

    @Operation(summary = "管理员审核变更申请：通过/拒绝")
    @PostMapping("/admin/review")
    @PreAuthorize("hasRole('ADMIN')")
    public Result review(@RequestBody ApproveDoctorChangeRequest req) {
        String adminUsername = SecurityUtils.getCurrentUsername();
        DoctorChangeRequestItem item = service.review(req, adminUsername);
        return Result.success(item);
    }
}



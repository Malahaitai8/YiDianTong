package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ScheduleSettingsDTO;
import com.example.springboot.service.ScheduleSettingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "排班设置", description = "号源上限与配置管理")
@RestController
@RequestMapping("/api/admin/schedule-settings")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class ScheduleSettingsController {

    @Resource
    private ScheduleSettingsService scheduleSettingsService;

    @Operation(summary = "获取全局排班设置")
    @GetMapping
    public Result getGlobalSettings() {
        return Result.success(scheduleSettingsService.getGlobalSettings());
    }

    @Operation(summary = "更新全局排班设置")
    @PutMapping
    public Result updateGlobalSettings(@RequestBody ScheduleSettingsDTO dto) {
        scheduleSettingsService.saveGlobalSettings(dto);
        return Result.success();
    }

    @Operation(summary = "获取医生排班设置")
    @GetMapping("/doctor/{doctorId}")
    public Result getDoctorSettings(@PathVariable Long doctorId) {
        return Result.success(scheduleSettingsService.getDoctorSettings(doctorId));
    }

    @Operation(summary = "更新医生排班设置")
    @PutMapping("/doctor/{doctorId}")
    public Result updateDoctorSettings(@PathVariable Long doctorId,
                                       @RequestBody ScheduleSettingsDTO dto) {
        scheduleSettingsService.saveDoctorSettings(doctorId, dto);
        return Result.success();
    }

    @Operation(summary = "获取门诊排班设置")
    @GetMapping("/clinic/{clinicId}")
    public Result getClinicSettings(@PathVariable Long clinicId) {
        return Result.success(scheduleSettingsService.getClinicSettings(clinicId));
    }

    @Operation(summary = "更新门诊排班设置")
    @PutMapping("/clinic/{clinicId}")
    public Result updateClinicSettings(@PathVariable Long clinicId,
                                       @RequestBody ScheduleSettingsDTO dto) {
        scheduleSettingsService.saveClinicSettings(clinicId, dto);
        return Result.success();
    }
}


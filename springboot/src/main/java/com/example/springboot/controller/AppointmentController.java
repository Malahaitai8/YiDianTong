package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Appointment;
import com.example.springboot.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import com.example.springboot.config.SecurityUtils;

@Tag(name = "预约管理", description = "预约挂号相关接口")
@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;


    @Operation(summary = "查询所有预约", description = "获取系统中所有预约记录")
    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Appointment> list = appointmentService.selectAll();

        return Result.success(list);
    }

    @Operation(summary = "根据ID查询预约", description = "通过预约ID获取预约详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {

        Appointment appointment = appointmentService.selectById(id);
        return Result.success(appointment);
    }

    @Operation(summary = "创建预约", description = "患者创建新的预约记录，需要登录")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    public Result create(@RequestBody Appointment appointment) {
        // 补充：获取当前患者 ID 并写入实体
        Long patientId = SecurityUtils.getCurrentUserId();
        appointment.setPatientId(patientId);
        appointment.setStatus("PENDING");
        Appointment saved = appointmentService.create(appointment);
        return Result.success(saved);
    }

    @Operation(summary = "删除预约", description = "删除指定预约记录（仅限本人或管理员）")
    @SecurityRequirement(name = "bearer-jwt")
    @DeleteMapping("/{id}")
    public Result delete(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {
        appointmentService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "取消预约", description = "取消指定预约（仅限本人）")
    @SecurityRequirement(name = "bearer-jwt")
    @PutMapping("/{id}/cancel")
    public Result cancel(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {
        appointmentService.cancelById(id);
        return Result.success();
    }

    @Operation(summary = "我的预约", description = "获取当前登录患者的所有预约记录")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    public Result myAppointments() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return Result.success(appointmentService.listByPatient(patientId));
    }
}


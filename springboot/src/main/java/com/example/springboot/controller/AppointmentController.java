package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Schedule; // <-- [新增] 导入
import com.example.springboot.mapper.ScheduleMapper; // <-- [新增] 导入
import com.example.springboot.service.AppointmentService;
import com.example.springboot.dto.CreateAppointmentRequest; // <-- [新增] 导入
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize; // <-- [新增] 导入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal; // <-- [新增] 导入
import java.util.List;
import com.example.springboot.config.SecurityUtils;

@Tag(name = "预约管理", description = "预约挂号相关接口")
@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private ScheduleMapper scheduleMapper; // <-- [新增] 注入

    @Operation(summary = "查询所有预约", description = "获取系统中所有预约记录")
    @GetMapping("/selectAll")
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 权限
    public Result selectAll() {
        List<Appointment> list = appointmentService.selectAll();
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询预约", description = "通过预约ID获取预约详情")
    @GetMapping("/selectById/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')") // <-- [新增] 权限
    public Result selectById(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {
        // TODO: Service层应检查是否为本人操作
        Appointment appointment = appointmentService.selectById(id);
        return Result.success(appointment);
    }

    @Operation(summary = "创建预约", description = "患者创建新的预约记录，需要登录")
    @SecurityRequirement(name = "bearer-jwt")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')") // <-- [新增] 权限
    public Result create(@jakarta.validation.Valid @RequestBody CreateAppointmentRequest request) { // <-- [修改] 签名

        // 1. 获取用户信息
        Long patientId = SecurityUtils.getCurrentUserId();
        // (TODO: 缺少获取 Patient 详细信息以计算报销比例)

        // 2. 查询排班信息 (获取 DoctorId 和 Fee)
        Schedule schedule = scheduleMapper.selectById(request.getScheduleId());
        if (schedule == null) {
            return Result.error("排班不存在");
        }
        // (校验号源的逻辑移至 Service 层)

        // 3. 构造 Appointment 实体
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setScheduleId(request.getScheduleId());

        // 4. [修复] 填充排班信息
        appointment.setDoctorId(schedule.getDoctorId());
        appointment.setAppointmentTime(request.getAppointmentTime()); // 使用前端传入的精确时间

        // 5. [修复] 填充费用 (TODO: 业务逻辑缺失)
        // 假设费用在 Schedule 表中 (但 DB 实体没有)
        // 暂设为 0
        appointment.setFee(new BigDecimal("0.00"));
        // (TODO: 缺少计算报销比例的逻辑)
        appointment.setActualFee(new BigDecimal("0.00"));

        appointment.setStatus("PENDING");
        appointment.setSourceType("ONLINE");
        appointment.setCreatedAt(new java.util.Date());

        try {
            // 6. [修复] 调用 Service (Service 负责扣减号源)
            Appointment saved = appointmentService.create(appointment);
            return Result.success(saved);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "删除预约", description = "删除指定预约记录（仅限本人或管理员）")
    @SecurityRequirement(name = "bearer-jwt")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')") // <-- [新增] 权限
    public Result delete(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {
        // TODO: Service层应检查是否为本人操作
        appointmentService.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "取消预约", description = "取消指定预约（仅限本人）")
    @SecurityRequirement(name = "bearer-jwt")
    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')") // <-- [新增] 权限
    public Result cancel(
            @Parameter(description = "预约ID", required = true) @PathVariable Long id) {
        // TODO: Service层应检查是否为本人操作
        appointmentService.cancelById(id);
        return Result.success();
    }

    @Operation(summary = "我的预约", description = "获取当前登录患者的所有预约记录")
    @SecurityRequirement(name = "bearer-jwt")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')") // <-- [新增] 权限
    public Result myAppointments() {
        Long patientId = SecurityUtils.getCurrentUserId();
        return Result.success(appointmentService.listByPatient(patientId));
    }
}
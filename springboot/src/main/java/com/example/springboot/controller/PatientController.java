package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ChangePasswordRequest;
import com.example.springboot.dto.UpdatePatientProfileRequest;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import com.example.springboot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "患者管理", description = "患者信息管理相关接口")
@RestController
@RequestMapping("/patient")
@SecurityRequirement(name = "bearer-jwt")
public class PatientController {

    @Resource
    private PatientService patientService;
    
    @Resource
    private UserService userService;

    @Operation(summary = "查询所有患者", description = "仅管理员可查询所有患者信息")
    @GetMapping("/selectAll")
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result selectAll() {
        List<Patient> list = patientService.selectAll();
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询患者", description = "管理员或本人可查看患者详情")
    @GetMapping("/selectById/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Result selectById(
            @Parameter(description = "患者ID", required = true) @PathVariable Long id) {
        Patient patient = patientService.selectById(id); // Service层已实现本人权限校验
        return Result.success(patient);
    }

    @Operation(summary = "新增患者", description = "仅管理员可创建患者记录")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员 (假设)
    public Result create(@jakarta.validation.Valid @RequestBody Patient patient) {
        patientService.create(patient);
        return Result.success();
    }

    @Operation(summary = "更新患者", description = "管理员或本人可更新患者信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public Result update(
            @Parameter(description = "患者ID", required = true) @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Patient patient) {
        patient.setId(id); // 使用路径参数中的ID
        patientService.update(patient); // Service层已实现本人权限校验
        return Result.success();
    }

    @Operation(summary = "删除患者", description = "仅管理员可删除患者记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result delete(
            @Parameter(description = "患者ID", required = true) @PathVariable Long id) {
        patientService.delete(id);
        return Result.success();
    }
    
    // ==================== 患者端个人信息管理接口 ====================
    
    @Operation(summary = "获取当前患者个人信息", description = "获取当前登录患者的个人信息（包括用户信息和患者信息）")
    @GetMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public Result getProfile() {
        try {
            Patient patient = patientService.getCurrentPatientProfile();
            return Result.success(patient);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "更新当前患者个人信息", description = "更新当前登录患者的个人信息（只能更新手机号等允许的字段，不能修改认证相关字段）")
    @PutMapping("/profile")
    @PreAuthorize("hasRole('PATIENT')")
    public Result updateProfile(@jakarta.validation.Valid @RequestBody UpdatePatientProfileRequest request) {
        try {
            patientService.updateCurrentPatientProfile(request.getPhoneNumber());
            return Result.success("个人信息更新成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @Operation(summary = "修改密码", description = "修改当前登录患者的密码（需要验证旧密码）")
    @PutMapping("/password")
    @PreAuthorize("hasRole('PATIENT')")
    public Result changePassword(@jakarta.validation.Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getOldPassword(), request.getNewPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    // ==================== 管理员审核患者认证状态接口 ====================
    
    @Operation(summary = "审核患者认证状态", description = "管理员审核患者身份认证，将状态从pending更新为verified")
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public Result approvePatient(
            @Parameter(description = "患者ID", required = true) @PathVariable Long id) {
        try {
            Patient patient = patientService.selectById(id);
            if (patient == null) {
                return Result.error("患者不存在");
            }
            
            // 更新认证状态为已认证
            patient.setIdStatus("verified");
            patientService.update(patient);
            
            return Result.success("患者认证审核成功");
        } catch (Exception e) {
            return Result.error("审核失败: " + e.getMessage());
        }
    }
}
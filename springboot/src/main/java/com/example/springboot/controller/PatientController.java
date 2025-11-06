package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize; // <-- [新增] 导入
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "患者管理", description = "患者信息管理相关接口")
@RestController
@RequestMapping("/patient")
@SecurityRequirement(name = "bearer-jwt")
public class PatientController {

    @Resource
    private PatientService patientService;

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
}
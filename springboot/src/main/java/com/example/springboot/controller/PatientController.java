package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
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

    @Operation(summary = "查询所有患者", description = "获取系统中所有患者信息")
    @GetMapping("/selectAll")
    public Result selectAll() {
        List<Patient> list = patientService.selectAll();
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询患者", description = "通过患者ID获取患者详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(
            @Parameter(description = "患者ID", required = true) @PathVariable Long id) {
        Patient patient = patientService.selectById(id);
        return Result.success(patient);
    }

    @Operation(summary = "新增患者", description = "创建新的患者记录")
    @PostMapping
    public Result create(@RequestBody Patient patient) {
        patientService.create(patient);
        return Result.success();
    }

    @Operation(summary = "更新患者", description = "更新患者信息")
    @PutMapping
    public Result update(@RequestBody Patient patient) {
        patientService.update(patient);
        return Result.success();
    }

    @Operation(summary = "删除患者", description = "删除指定患者记录")
    @DeleteMapping("/{id}")
    public Result delete(
            @Parameter(description = "患者ID", required = true) @PathVariable Long id) {
        patientService.delete(id);
        return Result.success();
    }
}



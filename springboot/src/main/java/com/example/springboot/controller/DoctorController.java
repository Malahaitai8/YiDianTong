package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Doctor;
import com.example.springboot.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize; // <-- [新增] 导入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "医生管理", description = "医生信息管理相关接口")
@RestController
@RequestMapping("/doctor")
@SecurityRequirement(name = "bearer-jwt")
public class DoctorController {

    @Resource
    private DoctorService doctorService;


    @Operation(summary = "查询所有医生", description = "获取系统中所有医生信息")
    @GetMapping("/selectAll")
    @PreAuthorize("isAuthenticated()") // <-- [新增] 任何登录用户
    public Result selectAll() {
        List<Doctor> list = doctorService.selectAll();
        return Result.success(list);
    }

    @Operation(summary = "根据ID查询医生", description = "通过医生ID获取医生详情")
    @GetMapping("/selectById/{id}")
    @PreAuthorize("isAuthenticated()") // <-- [新增] 任何登录用户
    public Result selectById(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id) {
        Doctor doctor = doctorService.selectById(id);
        return Result.success(doctor);
    }

    @Operation(summary = "新增医生", description = "仅管理员可创建医生记录")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result create(@jakarta.validation.Valid @RequestBody Doctor doctor) {
        doctorService.create(doctor);
        return Result.success();
    }

    @Operation(summary = "更新医生", description = "管理员或医生本人可更新医生信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR')") //管理员或医生本人 (Service层应做ID校验)
    public Result update(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id,
            @jakarta.validation.Valid @RequestBody Doctor doctor) {
        // TODO: Service层应检查是否为本人操作
        doctor.setId(id); // 使用路径参数中的ID
        doctorService.update(doctor);
        return Result.success();
    }

    @Operation(summary = "删除医生", description = "仅管理员可删除医生记录")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 仅管理员
    public Result delete(
            @Parameter(description = "医生ID", required = true) @PathVariable Long id) {
        doctorService.delete(id);
        return Result.success();
    }
}
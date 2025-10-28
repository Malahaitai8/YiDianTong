package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Clinic;
import com.example.springboot.service.ClinicService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "门诊管理", description = "门诊信息管理接口（查询对所有登录用户开放，新增/修改/删除需管理员权限）")
@RestController
@RequestMapping("/clinic")
@SecurityRequirement(name = "bearer-jwt")
public class ClinicController {

    @Resource
    private ClinicService clinicService;


    @Operation(summary = "查询所有门诊", description = "获取系统中所有门诊信息")
    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Clinic> list = clinicService.selectAll();

        return Result.success(list);
    }

    @Operation(summary = "根据ID查询门诊", description = "通过门诊ID获取门诊详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Clinic clinic = clinicService.selectById(id);
        return Result.success(clinic);
    }

    /**
     * 创建门诊（管理员）
     */
    @PostMapping
    @Operation(summary = "创建门诊", description = "仅管理员可创建门诊")
    @PreAuthorize("hasRole('ADMIN')")
    public Result create(@RequestBody Clinic clinic) {
        clinicService.create(clinic);
        return Result.success();
    }

    /**
     * 更新门诊（管理员）
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新门诊", description = "仅管理员可更新门诊")
    @PreAuthorize("hasRole('ADMIN')")
    public Result update(@PathVariable Long id, @RequestBody Clinic clinic) {
        clinic.setId(id);
        clinicService.update(clinic);
        return Result.success();
    }

    /**
     * 删除门诊（管理员）
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除门诊", description = "仅管理员可删除门诊")
    @PreAuthorize("hasRole('ADMIN')")
    public Result delete(@PathVariable Long id) {
        clinicService.delete(id);
        return Result.success();
    }

}


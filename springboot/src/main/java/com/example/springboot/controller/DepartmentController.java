package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Department;
import com.example.springboot.service.DepartmentService;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@Tag(name = "科室管理", description = "科室信息管理相关接口")
@RestController
@RequestMapping("/department")
@SecurityRequirement(name = "bearer-jwt")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;


    @Operation(summary = "查询所有科室", description = "获取系统中所有科室信息")
    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Department> list = departmentService.selectAll();

        return Result.success(list);
    }

    @Operation(summary = "根据ID查询科室", description = "通过科室ID获取科室详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Department department = departmentService.selectById(id);
        return Result.success(department);
    }

    /**
     * 创建科室
     */
    @PostMapping
    @Operation(summary = "创建科室", description = "仅管理员可创建科室")
    @PreAuthorize("hasRole('ADMIN')")
    public Result create(@RequestBody Department department) {
        departmentService.create(department);
        return Result.success();
    }

    /**
     * 更新科室
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新科室", description = "仅管理员可更新科室")
    @PreAuthorize("hasRole('ADMIN')")
    public Result update(@PathVariable Long id, @RequestBody Department department) {
        department.setId(id); // 使用路径参数中的ID
        departmentService.update(department);
        return Result.success();
    }

    /**
     * 删除科室
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除科室", description = "仅管理员可删除科室")
    @PreAuthorize("hasRole('ADMIN')")
    public Result delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}


package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Department;
import com.example.springboot.service.DepartmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Department> list = departmentService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Department department = departmentService.selectById(id);
        return Result.success(department);
    }

    /**
     * 创建科室
     */
    @PostMapping
    public Result create(@RequestBody Department department) {
        departmentService.create(department);
        return Result.success();
    }

    /**
     * 更新科室
     */
    @PutMapping
    public Result update(@RequestBody Department department) {
        departmentService.update(department);
        return Result.success();
    }

    /**
     * 删除科室
     */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        departmentService.delete(id);
        return Result.success();
    }
}


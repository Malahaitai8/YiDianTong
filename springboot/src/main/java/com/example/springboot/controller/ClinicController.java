package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Clinic;
import com.example.springboot.service.ClinicService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "门诊管理", description = "门诊信息查询相关接口")
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

}


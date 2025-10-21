package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.service.SystemConfigService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "系统配置", description = "系统配置查询相关接口")
@RestController
@RequestMapping("/systemConfig")
@SecurityRequirement(name = "bearer-jwt")
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;


    @Operation(summary = "查询所有配置", description = "获取系统中所有配置项")
    @GetMapping("/selectAll")
    public Result selectAll() {

        List<SystemConfig> list = systemConfigService.selectAll();

        return Result.success(list);
    }

    @Operation(summary = "根据ID查询配置", description = "通过配置ID获取详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        SystemConfig systemConfig = systemConfigService.selectById(id);
        return Result.success(systemConfig);
    }
    
    @Operation(summary = "根据Key查询配置", description = "通过配置Key获取详情")
    @GetMapping("/selectByKey/{key}")
    public Result selectByKey(@PathVariable String key) {

        SystemConfig systemConfig = systemConfigService.selectByKey(key);
        return Result.success(systemConfig);
    }

}


package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.service.SystemConfigService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/systemConfig")
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<SystemConfig> list = systemConfigService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        SystemConfig systemConfig = systemConfigService.selectById(id);
        return Result.success(systemConfig);
    }
    
    @GetMapping("/selectByKey/{key}")
    public Result selectByKey(@PathVariable String key) {

        SystemConfig systemConfig = systemConfigService.selectByKey(key);
        return Result.success(systemConfig);
    }

}


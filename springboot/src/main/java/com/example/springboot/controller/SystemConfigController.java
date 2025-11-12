package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.service.SystemConfigService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashMap;
import com.example.springboot.dto.FeesUpdateRequest;

@Tag(name = "系统配置", description = "系统配置查询相关接口")
@RestController
@RequestMapping({"/systemConfig", "/api/systemConfig"})
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

    /** 兼容：GET /systemConfig 与 /api/systemConfig 直接返回全部配置 */
    @Operation(summary = "查询所有配置(REST-风格别名)", description = "兼容前端调用 GET /api/systemConfig")
    @GetMapping({"", "/"})
    public Result selectAllAlias() {
        return selectAll();
    }

    @Operation(summary = "根据ID查询配置", description = "通过配置ID获取详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        SystemConfig systemConfig = systemConfigService.selectById(id);
        return Result.success(systemConfig);
    }
    
    /** 兼容：GET /systemConfig/{id} */
    @Operation(summary = "根据ID查询配置(REST-风格别名)", description = "兼容前端调用 GET /api/systemConfig/{id}")
    @GetMapping("/{id}")
    public Result selectByIdRest(@PathVariable Long id) {
        return selectById(id);
    }

    @Operation(summary = "根据Key查询配置", description = "通过配置Key获取详情")
    @GetMapping("/selectByKey/{key}")
    public Result selectByKey(@PathVariable String key) {

        SystemConfig systemConfig = systemConfigService.selectByKey(key);
        return Result.success(systemConfig);
    }

    /** 兼容：GET /systemConfig/key/{key} */
    @Operation(summary = "根据Key查询配置(REST-风格别名)", description = "兼容前端调用 GET /api/systemConfig/key/{key}")
    @GetMapping("/key/{key}")
    public Result selectByKeyRest(@PathVariable String key) {
        return selectByKey(key);
    }

    @Operation(summary = "根据Key更新配置", description = "通过配置Key更新其value值")
    @PutMapping("/updateByKey/{key}")
    public Result updateByKey(@PathVariable String key, @RequestBody com.example.springboot.dto.ConfigUpdateRequest request) {
        int updated = systemConfigService.updateValueByKey(key, request.getValue());
        if (updated > 0) {
            return Result.success();
        }
        return Result.error("更新失败或未找到对应配置");
    }

    /** 兼容：PUT /systemConfig/key/{key} */
    @Operation(summary = "根据Key更新配置(REST-风格别名)", description = "兼容前端调用 PUT /api/systemConfig/key/{key}")
    @PutMapping("/key/{key}")
    public Result updateByKeyRest(@PathVariable String key, @RequestBody com.example.springboot.dto.ConfigUpdateRequest request) {
        return updateByKey(key, request);
    }

    @Operation(summary = "根据Key删除配置", description = "通过配置Key删除配置项")
    @DeleteMapping("/deleteByKey/{key}")
    public Result deleteByKey(@PathVariable String key) {
        int deleted = systemConfigService.deleteByKey(key);
        if (deleted > 0) {
            return Result.success();
        }
        return Result.error("删除失败或未找到对应配置");
    }

    /** 兼容：DELETE /systemConfig/key/{key} */
    @Operation(summary = "根据Key删除配置(REST-风格别名)", description = "兼容前端调用 DELETE /api/systemConfig/key/{key}")
    @DeleteMapping("/key/{key}")
    public Result deleteByKeyRest(@PathVariable String key) {
        return deleteByKey(key);
    }

    @Operation(summary = "获取号别费用配置", description = "返回 normal/expert/vip 三类费用配置")
    @GetMapping("/fees")
    @PreAuthorize("hasRole('ADMIN')")
    public Result getFees() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("normal", systemConfigService.getDecimalOrDefault("FEE_NORMAL", new BigDecimal("0.00")));
        map.put("expert", systemConfigService.getDecimalOrDefault("FEE_EXPERT", new BigDecimal("0.00")));
        map.put("vip", systemConfigService.getDecimalOrDefault("FEE_VIP", new BigDecimal("0.00")));
        return Result.success(map);
    }

    @Operation(summary = "更新号别费用配置", description = "按需更新 normal/expert/vip 的费用，不传的保持不变")
    @PutMapping("/fees")
    @PreAuthorize("hasRole('ADMIN')")
    public Result updateFees(@RequestBody FeesUpdateRequest request) {
        int changed = 0;
        if (request.getNormal() != null) {
            changed += systemConfigService.updateValueByKey("FEE_NORMAL", request.getNormal().toPlainString());
        }
        if (request.getExpert() != null) {
            changed += systemConfigService.updateValueByKey("FEE_EXPERT", request.getExpert().toPlainString());
        }
        if (request.getVip() != null) {
            changed += systemConfigService.updateValueByKey("FEE_VIP", request.getVip().toPlainString());
        }
        return changed > 0 ? Result.success() : Result.error("未更新任何配置");
    }

}


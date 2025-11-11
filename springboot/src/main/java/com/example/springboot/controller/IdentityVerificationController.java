package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.VerifyIdentityRequest;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.IdentityVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.annotation.Resource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 身份认证控制器
 */
@Tag(name = "身份认证", description = "患者身份认证相关接口")
@RestController
@RequestMapping("/auth/verify")
@SecurityRequirement(name = "bearer-jwt")
public class IdentityVerificationController {

    @Resource
    private IdentityVerificationService identityVerificationService;

    @Operation(summary = "身份认证", description = "患者提交姓名、学号/工号、身份证进行身份认证。系统会根据学号/工号在白名单中查找，确定用户角色（学生/教师/校外人员）")
    @PostMapping("/identity")
    @PreAuthorize("hasRole('PATIENT')")
    public Result verifyIdentity(@jakarta.validation.Valid @RequestBody VerifyIdentityRequest request) {
        try {
            Patient patient = identityVerificationService.verifyIdentity(request);
            Result result = Result.success(patient);
            result.setMsg("身份认证成功");
            return result;
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "查询当前用户认证状态", description = "查询当前登录患者的认证状态和信息")
    @GetMapping("/status")
    @PreAuthorize("hasRole('PATIENT')")
    public Result getVerificationStatus() {
        try {
            Long currentUserId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                return Result.error("未登录");
            }
            Patient patient = identityVerificationService.getPatientByUserId(currentUserId);
            if (patient == null) {
                return Result.error("患者信息不存在");
            }
            return Result.success(patient);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}


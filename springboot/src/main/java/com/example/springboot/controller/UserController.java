package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ChangePasswordRequest;
import com.example.springboot.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * 处理所有用户（患者、医生、管理员）的通用功能
 */
@Tag(name = "用户管理", description = "用户通用功能接口")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "修改密码", description = "修改当前登录用户的密码（需要验证旧密码），适用于所有角色")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "密码修改成功"),
        @ApiResponse(responseCode = "400", description = "旧密码不正确或新密码格式不正确"),
        @ApiResponse(responseCode = "401", description = "未登录")
    })
    @PostMapping("/changePassword")
    @PreAuthorize("isAuthenticated()")
    public Result changePassword(@jakarta.validation.Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(request.getOldPassword(), request.getNewPassword());
            return Result.success("密码修改成功");
        } catch (Exception e) {
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.trim().isEmpty()) {
                errorMsg = "密码修改失败";
            }
            return Result.error(errorMsg);
        }
    }
}

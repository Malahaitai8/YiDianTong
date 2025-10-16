package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Admin;
import com.example.springboot.service.AdminService;
import com.example.springboot.dto.DoctorCreateRequest;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import com.example.springboot.dto.ResetPasswordRequest;

@Tag(name = "管理员管理", description = "管理员相关功能接口")
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearer-jwt")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;


    @Operation(summary = "查询所有管理员", description = "管理员列表")
    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Admin> list = adminService.selectAll();

        return Result.success(list);
    }

    @Operation(summary = "根据ID查询管理员", description = "管理员详情")
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    /**
     * 管理员创建医生账号
     */
    @Operation(summary = "创建医生账号", description = "为医生创建登录账号")
    @PostMapping("/doctor/create")
    public Result createDoctor(@jakarta.validation.Valid @RequestBody DoctorCreateRequest request) {
        // 检查用户名重复
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            return Result.error("用户名已存在");
        }
        // 创建医生账号
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("doctor");
        // 初始状态待审批，可根据业务需要改为active
        user.setStatus("active");
        userMapper.insert(user);
        return Result.success("医生账号创建成功");
    }

    /**
     * 停用医生账号
     */
    @Operation(summary = "停用医生账号", description = "将医生账号状态设置为disabled")
    @PostMapping("/doctor/{id}/disable")
    public Result disableDoctor(@PathVariable Long id) {
        userMapper.updateStatus(id, "disabled");
        return Result.success("医生账号已停用");
    }

    /**
     * 重置医生密码
     */
    @Operation(summary = "重置医生密码", description = "重置指定医生的登录密码")
    @PostMapping("/doctor/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id, @jakarta.validation.Valid @RequestBody ResetPasswordRequest request) {
        userMapper.updatePassword(id, passwordEncoder.encode(request.getNewPassword()));
        return Result.success("密码已重置");
    }

    /**
     * 管理员将所有用户密码重置为 123456
     */
    @Operation(summary = "重置所有用户密码", description = "管理员一键重置所有用户密码为123456")
    @PostMapping("/users/reset-all-passwords")
    public Result resetAllPasswords() {
        String encoded = passwordEncoder.encode("123456");
        int affected = userMapper.updateAllPasswords(encoded);
        return Result.success("已重置密码的用户数量: " + affected);
    }
}


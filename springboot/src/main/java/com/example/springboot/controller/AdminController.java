package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Admin;
import com.example.springboot.service.AdminService;
import com.example.springboot.dto.DoctorCreateRequest;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import com.example.springboot.dto.ResetPasswordRequest;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Admin> list = adminService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Admin admin = adminService.selectById(id);
        return Result.success(admin);
    }

    /**
     * 管理员创建医生账号
     */
    @PostMapping("/doctor/create")
    public Result createDoctor(@RequestBody DoctorCreateRequest request) {
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
    @PostMapping("/doctor/{id}/disable")
    public Result disableDoctor(@PathVariable Long id) {
        userMapper.updateStatus(id, "disabled");
        return Result.success("医生账号已停用");
    }

    /**
     * 重置医生密码
     */
    @PostMapping("/doctor/{id}/reset-password")
    public Result resetPassword(@PathVariable Long id, @RequestBody ResetPasswordRequest request) {
        userMapper.updatePassword(id, passwordEncoder.encode(request.getNewPassword()));
        return Result.success("密码已重置");
    }
}


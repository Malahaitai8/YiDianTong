package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.CustomUserDetails;
import com.example.springboot.config.JwtUtil;
import com.example.springboot.dto.LoginRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.RegisterRequest;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 处理登录、登出等认证相关请求
 */
@Tag(name = "认证管理", description = "用户登录、注册、登出相关接口")
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "统一登录", description = "支持患者、医生、管理员三种角色登录，返回JWT Token")
    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest loginRequest) {
        try {
            // 使用Spring Security进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );

            // 获取认证后的用户详情
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // 检查账户状态
            if (!"active".equals(user.getStatus())) {
                return Result.error("账户状态异常，无法登录");
            }

            // 生成JWT token
            String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                user.getRole()
            );

            // 构建响应
            LoginResponse response = new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getStatus()
            );

            return Result.success(response);

        } catch (BadCredentialsException e) {
            return Result.error("用户名或密码错误");
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    @Operation(summary = "患者登录", description = "患者专用登录接口（兼容性接口）")
    @PostMapping("/patient/login")
    public Result patientLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @Operation(summary = "医生登录", description = "医生专用登录接口（兼容性接口）")
    @PostMapping("/doctor/login")
    public Result doctorLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @Operation(summary = "管理员登录", description = "管理员专用登录接口（兼容性接口）")
    @PostMapping("/admin/login")
    public Result adminLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @Operation(summary = "用户注册", description = "患者自助注册接口，密码会自动加密")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.selectByUsername(registerRequest.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 仅允许患者自助注册
            // 仅允许患者自助注册
            String reqRole = registerRequest.getRole();
            if (reqRole != null && !"patient".equals(reqRole)) {
                return Result.error("仅患者账号允许自助注册");
            }

            // 角色固定为 patient
            String role = "patient";

            // 创建新用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            // 密码加密
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(role);
            // 患者直接激活
            user.setStatus("active");

            userMapper.insert(user);

            return Result.success("注册成功");

        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @Operation(summary = "用户登出", description = "登出接口，JWT是无状态的，主要由前端删除token")
    @PostMapping("/logout")
    public Result logout() {
        // 清除SecurityContext
        // SecurityContextHolder.clearContext();
        return Result.success("登出成功");
    }
}


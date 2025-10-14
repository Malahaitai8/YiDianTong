package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.CustomUserDetails;
import com.example.springboot.config.JwtUtil;
import com.example.springboot.dto.LoginRequest;
import com.example.springboot.dto.LoginResponse;
import com.example.springboot.dto.RegisterRequest;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
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

    /**
     * 统一登录接口
     * 支持patient、doctor、admin三种角色登录
     */
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

    /**
     * 患者登录接口（兼容性，实际调用统一登录）
     */
    @PostMapping("/patient/login")
    public Result patientLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    /**
     * 医生登录接口（兼容性，实际调用统一登录）
     */
    @PostMapping("/doctor/login")
    public Result doctorLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    /**
     * 管理员登录接口（兼容性，实际调用统一登录）
     */
    @PostMapping("/admin/login")
    public Result adminLogin(@RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    /**
     * 用户注册接口
     * 注册时密码会自动加密
     */
    @PostMapping("/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 检查用户名是否已存在
            User existingUser = userMapper.selectByUsername(registerRequest.getUsername());
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 验证角色
            String role = registerRequest.getRole();
            if (!role.equals("patient") && !role.equals("doctor") && !role.equals("admin")) {
                return Result.error("无效的角色类型");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(registerRequest.getUsername());
            // 密码加密
            user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            user.setRole(role);
            // 根据角色设置不同的初始状态
            if ("doctor".equals(role) || "admin".equals(role)) {
                user.setStatus("pending_approval");  // 医生和管理员需要审批
            } else {
                user.setStatus("active");  // 患者直接激活
            }

            userMapper.insert(user);

            return Result.success("注册成功");

        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    /**
     * 登出接口
     * JWT是无状态的，登出主要由前端处理（删除本地token）
     * 后端可以记录登出日志或将token加入黑名单
     */
    @PostMapping("/logout")
    public Result logout() {
        // 清除SecurityContext
        // SecurityContextHolder.clearContext();
        return Result.success("登出成功");
    }
}


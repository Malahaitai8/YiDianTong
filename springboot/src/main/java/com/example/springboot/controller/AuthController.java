package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.config.CustomUserDetails;
import com.example.springboot.config.JwtUtil;
import com.example.springboot.constants.RoleConstants;
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

    @Autowired
    private com.example.springboot.mapper.PatientMapper patientMapper;

    @Operation(summary = "统一登录", description = "支持患者、医生、管理员三种角色登录，返回JWT Token")
    @PostMapping("/login")
    public Result login(@jakarta.validation.Valid @RequestBody LoginRequest loginRequest) {
        try {
            // 去除用户名和密码的首尾空格，避免因空格导致的认证失败
            String username = loginRequest.getUsername() != null ? loginRequest.getUsername().trim() : null;
            String password = loginRequest.getPassword() != null ? loginRequest.getPassword().trim() : null;
            
            // 使用Spring Security进行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    username,
                    password
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
    public Result patientLogin(@jakarta.validation.Valid @RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @Operation(summary = "医生登录", description = "医生专用登录接口（兼容性接口）")
    @PostMapping("/doctor/login")
    public Result doctorLogin(@jakarta.validation.Valid @RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @Operation(summary = "管理员登录", description = "管理员专用登录接口（兼容性接口）")
    @PostMapping("/admin/login")
    public Result adminLogin(@jakarta.validation.Valid @RequestBody LoginRequest loginRequest) {
        return login(loginRequest);
    }

    @Operation(summary = "患者注册", description = "仅限患者自助注册，医生和管理员账号需由管理员创建")
    @PostMapping("/register")
    public Result register(@jakarta.validation.Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // 去除用户名和手机号的首尾空格
            String username = registerRequest.getUsername() != null ? registerRequest.getUsername().trim() : null;
            String phoneNumber = registerRequest.getPhoneNumber() != null ? registerRequest.getPhoneNumber().trim() : null;
            
            // 检查用户名是否已存在
            User existingUser = userMapper.selectByUsername(username);
            if (existingUser != null) {
                return Result.error("用户名已存在");
            }

            // 严格限制：只允许患者自助注册
            String reqRole = registerRequest.getRole();
            if (reqRole != null && !RoleConstants.DB_ROLE_PATIENT.equals(reqRole)) {
                return Result.error("仅支持患者账号注册，医生和管理员账号请联系管理员创建");
            }

            // 即使请求中没有指定角色，也强制设为患者
            String role = RoleConstants.DB_ROLE_PATIENT;

            // 1. 创建用户账号
            User user = new User();
            user.setUsername(username);
            // 密码加密（密码也需要trim）
            String password = registerRequest.getPassword() != null ? registerRequest.getPassword().trim() : null;
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            // 患者直接激活
            user.setStatus("active");

            userMapper.insert(user);

            // 2. 创建患者详细信息（注册时只保存手机号，其他信息待认证）
            com.example.springboot.entity.Patient patient = new com.example.springboot.entity.Patient();
            patient.setUserId(user.getId());
            
            // 注册时只保存手机号，姓名、角色等信息在认证时填写
            patient.setPhoneNumber(phoneNumber);
            
            // 设置身份认证状态为待认证
            patient.setIdStatus("pending");
            
            // 其他字段（name, specificRole, idCardNumber, identityNumber）在认证时填写
            // 临时设置name为用户名，认证时会更新
            patient.setName(username);
            // 未认证时默认为外来人员，认证时根据白名单确定（可能更新为student或teacher）
            patient.setSpecificRole("outsider");

            patientMapper.insert(patient);

            return Result.success("患者注册成功");

        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 捕获数据库唯一索引冲突异常
            return Result.error("用户名已存在，请使用其他用户名");
        } catch (Exception e) {
            return Result.error("注册失败: " + e.getMessage());
        }
    }

    @Operation(
        summary = "用户登出", 
        description = "登出接口。由于JWT是无状态认证，服务端不保存token状态，因此：\n" +
                      "1. 前端需要删除本地存储的token（localStorage/sessionStorage）\n" +
                      "2. 前端需要清除相关的用户状态\n" +
                      "3. 服务端清除当前请求的安全上下文\n" +
                      "注意：token在过期前仍然有效，如需立即失效可实现token黑名单机制（Redis）"
    )
    @PostMapping("/logout")
    public Result logout() {
        try {
            // 清除当前请求的安全上下文
            org.springframework.security.core.context.SecurityContextHolder.clearContext();
            
            // 注：JWT是无状态的，服务端不保存token
            // 如需实现立即失效，可以：
            // 1. 将token加入Redis黑名单，过期时间与token一致
            // 2. 在JwtAuthenticationFilter中检查黑名单
            // 示例代码：
            // String token = request.getHeader("Authorization");
            // if (token != null) {
            //     redisTemplate.opsForValue().set("blacklist:" + token, "1", expiration, TimeUnit.MILLISECONDS);
            // }
            
            return Result.success("登出成功，请清除本地token");
        } catch (Exception e) {
            return Result.error("登出失败: " + e.getMessage());
        }
    }
}


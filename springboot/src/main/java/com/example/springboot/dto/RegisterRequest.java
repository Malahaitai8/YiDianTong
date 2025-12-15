package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 注册请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "username不能为空")
    @Size(min = 4, max = 20, message = "用户名长度必须在4-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    @NotBlank(message = "password不能为空")
    private String password;
    private String role;  // patient, doctor, admin
    // 以下为患者资料（可选），若不提供将使用合理默认值
    private String name;           // 真实姓名（缺省使用 username）
    private String specificRole;   // student/teacher（缺省为 student）
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;    // 手机号（可选）
    private String idCardNumber;   // 身份证号（可选）
    private String identityNumber; // 学号或工号（可选，根据specificRole判断：student=学号，teacher=工号）
}


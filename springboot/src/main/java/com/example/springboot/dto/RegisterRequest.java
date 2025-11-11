package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
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
    private String username;
    @NotBlank(message = "password不能为空")
    private String password;
    private String role;  // patient, doctor, admin
    // 以下为患者资料（可选），若不提供将使用合理默认值
    private String name;           // 真实姓名（缺省使用 username）
    private String specificRole;   // student/teacher（缺省为 student）
    private String phoneNumber;    // 手机号（可选）
    private String idCardNumber;   // 身份证号（可选）
    private String identityNumber; // 学号或工号（可选，根据specificRole判断：student=学号，teacher=工号）
}


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
}


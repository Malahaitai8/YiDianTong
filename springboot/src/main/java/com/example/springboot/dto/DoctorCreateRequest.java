package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 管理员创建医生账号请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorCreateRequest {
    @NotBlank(message = "username不能为空")
    private String username;
    @NotBlank(message = "password不能为空")
    private String password;
    @jakarta.validation.constraints.NotNull(message = "clinicId不能为空")
    private Long clinicId;
    @NotBlank(message = "name不能为空")
    @Size(min = 2, max = 15, message = "医生姓名长度必须在2-15个字符之间")
    private String name;
    private String title;
    private String specialty;
    private String bio;
}

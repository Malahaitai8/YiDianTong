package com.example.springboot.dto;

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
    private String username;
    private String password;
}

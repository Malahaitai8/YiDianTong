package com.example.springboot.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 白名单实体类
 * 用于存储学号/工号，用于验证用户身份
 */
@Data
public class Whitelist {
    private Long id;
    private String identityNumber;  // 学号或工号
    private String roleType;        // 角色类型：student/teacher/outsider
    private String status;          // 状态：active/inactive
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


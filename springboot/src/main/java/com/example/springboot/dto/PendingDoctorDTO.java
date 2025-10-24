package com.example.springboot.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 待审核医生信息DTO
 * 包含医生基本信息和用户账号信息
 */
@Data
public class PendingDoctorDTO {
    private Long userId;           // 用户ID
    private String username;       // 用户名
    private Long doctorId;         // 医生ID
    private String name;           // 医生姓名
    private String title;          // 职称
    private String specialty;      // 专长
    private String bio;            // 简介
    private Long clinicId;         // 所属门诊ID
    private String clinicName;     // 门诊名称
    private LocalDateTime createdAt; // 注册时间
    private String status;         // 用户状态
}


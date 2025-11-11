package com.example.springboot.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 患者更新个人信息请求DTO
 * 注意：认证相关字段（姓名、身份证、学号/工号、角色、认证状态）不能通过此接口修改
 */
@Data
public class UpdatePatientProfileRequest {
    
    /**
     * 手机号码（可选）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phoneNumber;
}


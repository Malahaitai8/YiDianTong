package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 身份认证请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyIdentityRequest {
    @NotBlank(message = "姓名不能为空")
    private String name;
    
    @NotBlank(message = "学号/工号不能为空")
    private String identityNumber;
    
    @NotBlank(message = "身份证号不能为空")
    private String idCardNumber;
}


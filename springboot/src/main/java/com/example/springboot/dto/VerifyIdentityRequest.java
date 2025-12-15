package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "^[\\u4e00-\\u9fa5a-zA-Z]+$", message = "真实姓名只能包含汉字或英文字母")
    private String name;
    
    @NotBlank(message = "学号/工号不能为空")
    private String identityNumber;
    
    @NotBlank(message = "身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$", message = "身份证号格式不正确")
    private String idCardNumber;
}


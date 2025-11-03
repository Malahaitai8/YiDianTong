package com.example.springboot.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 审核申请请求 DTO
 */
@Data
public class ReviewApplicationRequestDTO {
    
    /**
     * 申请ID
     */
    @NotNull(message = "申请ID不能为空")
    private Long requestId;
    
    /**
     * 审核结果 (APPROVED/REJECTED)
     */
    @NotBlank(message = "审核结果不能为空")
    @Pattern(regexp = "APPROVED|REJECTED", message = "审核结果只能是 APPROVED 或 REJECTED")
    private String action;
    
    /**
     * 拒绝原因 (拒绝时必填)
     */
    @Size(max = 500, message = "拒绝原因不能超过500字符")
    private String rejectReason;
}


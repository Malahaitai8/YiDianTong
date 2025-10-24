package com.example.springboot.dto;

import lombok.Data;

/**
 * 审核请求DTO
 * 用于审核通过/拒绝操作
 */
@Data
public class ApprovalRequest {
    private Long userId;      // 待审核的用户ID
    private String reason;    // 审核意见/原因（可选）
}


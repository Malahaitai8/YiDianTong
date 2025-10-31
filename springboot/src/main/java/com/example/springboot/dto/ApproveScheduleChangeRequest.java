package com.example.springboot.dto;

import lombok.Data;

/**
 * 管理员审核调班申请的请求DTO
 */
@Data
public class ApproveScheduleChangeRequest {
    
    /**
     * 申请ID (必填)
     */
    private String id;
    
    /**
     * 审核动作 (必填，APPROVE/REJECT)
     */
    private String action;
    
    /**
     * 拒绝原因 (当action=REJECT时建议填写)
     */
    private String reason;
}


package com.example.springboot.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建候补预支付订单请求DTO
 */
@Data
public class CreateWaitlistPrepaymentRequest {
    
    /**
     * 排班ID
     */
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;
    
    /**
     * 候补记录ID（可选，若为空则自动创建）
     */
    private Long waitlistId;
}

package com.example.springboot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 退款预支付订单请求DTO
 */
@Data
public class RefundPrepaymentOrderRequest {
    
    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
    
    /**
     * 退款原因
     */
    private String reason;
}

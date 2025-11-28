package com.example.springboot.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 候补预支付订单实体类
 */
@Data
public class PrepaymentOrder {
    /**
     * 预支付订单唯一ID
     */
    private Long id;
    
    /**
     * 订单号（唯一）
     */
    private String orderNo;
    
    /**
     * 患者ID
     */
    private Long patientId;
    
    /**
     * 排班ID
     */
    private Long scheduleId;
    
    /**
     * 候补记录ID
     */
    private Long waitlistId;
    
    /**
     * 订单类型 (WAITLIST-候补预支付)
     */
    private String orderType;
    
    /**
     * 原始挂号费
     */
    private BigDecimal originalFee;
    
    /**
     * 实际支付费用（报销后）
     */
    private BigDecimal actualFee;
    
    /**
     * 已支付金额
     */
    private BigDecimal paidAmount;
    
    /**
     * 订单状态 (PENDING-待支付, PAID-已支付, REFUNDED-已退款, CONSUMED-已消费, EXPIRED-已过期)
     */
    private String status;
    
    /**
     * 支付方式 (ALIPAY, WECHAT, BALANCE等)
     */
    private String paymentMethod;
    
    /**
     * 支付时间
     */
    private Date paymentTime;
    
    /**
     * 退款时间
     */
    private Date refundTime;
    
    /**
     * 退款原因
     */
    private String refundReason;
    
    /**
     * 订单过期时间
     */
    private Date expireTime;
    
    /**
     * 创建时间
     */
    private Date createdAt;
    
    /**
     * 更新时间
     */
    private Date updatedAt;
}

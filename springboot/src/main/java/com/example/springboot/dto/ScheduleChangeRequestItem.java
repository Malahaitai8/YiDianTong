package com.example.springboot.dto;

import lombok.Data;

/**
 * 调班申请项（包含所有申请信息）
 */
@Data
public class ScheduleChangeRequestItem {
    
    /**
     * 申请唯一ID (UUID)
     */
    private String id;
    
    /**
     * 排班ID
     */
    private Long scheduleId;
    
    /**
     * 提交申请的用户ID
     */
    private Long requestedByUserId;
    
    /**
     * 医生ID
     */
    private Long doctorId;
    
    /**
     * 医生姓名 (用于展示)
     */
    private String doctorName;
    
    // ==================== 原始排班信息 ====================
    
    /**
     * 原排班日期
     */
    private String originalScheduleDate;
    
    /**
     * 原时间段
     */
    private String originalTimeSlot;
    
    /**
     * 原号别
     */
    private String originalSlotType;
    
    /**
     * 原总号源数
     */
    private Integer originalTotalSlots;
    
    // ==================== 新的排班信息 ====================
    
    /**
     * 新排班日期 (NULL表示不修改)
     */
    private String newScheduleDate;
    
    /**
     * 新时间段 (NULL表示不修改)
     */
    private String newTimeSlot;
    
    /**
     * 新号别 (NULL表示不修改)
     */
    private String newSlotType;
    
    /**
     * 新总号源数 (NULL表示不修改)
     */
    private Integer newTotalSlots;
    
    /**
     * 申请原因
     */
    private String reason;
    
    /**
     * 审核状态 (PENDING/APPROVED/REJECTED)
     */
    private String status;
    
    /**
     * 审核人用户名
     */
    private String approvedBy;
    
    /**
     * 审核时间 (时间戳毫秒)
     */
    private Long approvedAt;
    
    /**
     * 拒绝原因
     */
    private String rejectionReason;
    
    /**
     * 创建时间 (时间戳毫秒)
     */
    private Long createdAt;
    
    /**
     * 更新时间 (时间戳毫秒)
     */
    private Long updatedAt;
}


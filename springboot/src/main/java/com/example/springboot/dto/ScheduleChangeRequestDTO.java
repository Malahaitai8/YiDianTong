package com.example.springboot.dto;

import lombok.Data;

/**
 * 医生提交调班申请的请求DTO
 */
@Data
public class ScheduleChangeRequestDTO {
    
    /**
     * 要调整的排班ID (必填)
     */
    private Long scheduleId;
    
    /**
     * 提交申请的医生用户ID (由controller自动设置)
     */
    private Long requestedByUserId;
    
    /**
     * 医生ID (可选，会从requestedByUserId自动推导)
     */
    private Long doctorId;
    
    /**
     * 新的排班日期 (可选，格式: yyyy-MM-dd)
     */
    private String newScheduleDate;
    
    /**
     * 新的时间段 (可选，MORNING/AFTERNOON/EVENING)
     */
    private String newTimeSlot;
    
    /**
     * 新的号别 (可选，normal/expert/vip)
     */
    private String newSlotType;
    
    /**
     * 新的总号源数 (可选)
     */
    private Integer newTotalSlots;
    
    /**
     * 申请原因 (可选)
     */
    private String reason;
}


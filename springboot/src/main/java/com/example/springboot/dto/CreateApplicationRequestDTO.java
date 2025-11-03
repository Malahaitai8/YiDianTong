package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;

/**
 * 创建申请请求 DTO
 */
@Data
public class CreateApplicationRequestDTO {
    
    /**
     * 申请类型 (SCHEDULE_CHANGE/INFO_UPDATE)
     */
    @NotBlank(message = "申请类型不能为空")
    @Pattern(regexp = "SCHEDULE_CHANGE|INFO_UPDATE", message = "申请类型只能是 SCHEDULE_CHANGE 或 INFO_UPDATE")
    private String requestType;
    
    // ===== 调班申请相关字段 =====
    
    /**
     * 原排班ID (调班申请时必填)
     */
    private Long scheduleId;
    
    /**
     * 调班类型 (CANCEL/RESCHEDULE/ADJUST_SLOTS)
     */
    @Pattern(regexp = "CANCEL|RESCHEDULE|ADJUST_SLOTS", message = "调班类型只能是 CANCEL, RESCHEDULE, ADJUST_SLOTS")
    private String changeType;
    
    /**
     * 新出诊日期 (改期时使用)
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date newDate;
    
    /**
     * 新时间段 (改期时使用)
     */
    private String newTimeSlot;
    
    /**
     * 号源调整数量 (正数增加/负数减少)
     */
    private Integer slotAdjustment;
    
    // ===== 信息修改申请相关字段 =====
    
    /**
     * 要修改信息的医生ID (信息修改申请时必填)
     */
    private Long doctorId;
    
    /**
     * 要修改的字段名 (如: name/title/specialty/bio)
     */
    @Size(max = 100, message = "字段名不能超过100字符")
    private String fieldName;
    
    /**
     * 新值
     */
    private String newValue;
    
    // ===== 通用字段 =====
    
    /**
     * 申请理由
     */
    @NotBlank(message = "申请理由不能为空")
    @Size(max = 500, message = "申请理由不能超过500字符")
    private String reason;
}


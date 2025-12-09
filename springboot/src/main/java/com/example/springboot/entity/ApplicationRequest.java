package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 申请记录实体类
 * 对应数据库表：application_request
 */
@Data
public class ApplicationRequest {
    
    /**
     * 申请唯一ID
     */
    private Long id;
    
    /**
     * 申请类型 (SCHEDULE_CHANGE/INFO_UPDATE)
     */
    private String requestType;
    
    /**
     * 申请人用户ID
     */
    private Long applicantId;
    
    /**
     * 申请人角色 (doctor/admin)
     */
    private String applicantRole;
    
    /**
     * 申请人姓名（医生姓名）
     */
    private String applicantName;
    
    // ===== 调班申请相关字段 =====
    
    /**
     * 原排班ID (调班申请时使用)
     */
    private Long scheduleId;
    
    /**
     * 调班类型 (CANCEL/RESCHEDULE/ADJUST_SLOTS)
     */
    private String changeType;
    
    /**
     * 原出诊日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalDate;
    
    /**
     * 原时间段
     */
    private String originalTimeSlot;
    
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
     * 要修改信息的医生ID (信息修改申请时使用)
     */
    private Long doctorId;
    
    /**
     * 要修改的字段名 (如: name/title/specialty/bio)
     */
    private String fieldName;
    
    /**
     * 原值
     */
    private String oldValue;
    
    /**
     * 新值
     */
    private String newValue;
    
    // ===== 申请状态与处理 =====
    
    /**
     * 申请状态 (PENDING/APPROVED/REJECTED/CANCELLED)
     */
    private String status;
    
    /**
     * 申请理由
     */
    private String reason;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 审核人用户ID
     */
    private Long reviewerId;
    
    /**
     * 审核人姓名（管理员姓名）
     */
    private String reviewerName;
    
    /**
     * 审核时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewedAt;
    
    /**
     * 申请创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
    
    // 关联对象（用于返回详细信息）
    private User applicant;
    private User reviewer;
    private Schedule schedule;
    private Doctor doctor;
}


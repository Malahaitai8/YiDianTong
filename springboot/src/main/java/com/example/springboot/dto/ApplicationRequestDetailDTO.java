package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 申请记录详情 DTO
 */
@Data
public class ApplicationRequestDetailDTO {
    
    private Long id;
    private String requestType;
    private String requestTypeName; // 申请类型名称（中文）
    
    // 申请人信息
    private Long applicantId;
    private String applicantName; // 申请人姓名（医生姓名）
    private String applicantUsername;
    private String applicantRole;
    
    // 调班申请相关
    private Long scheduleId;
    private String changeType;
    private String changeTypeName; // 调班类型名称（中文）
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date originalDate;
    private String originalTimeSlot;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date newDate;
    private String newTimeSlot;
    
    private Integer slotAdjustment;
    
    // 信息修改申请相关
    private Long doctorId;
    private String doctorName;
    private String fieldName;
    private String fieldNameDisplay; // 字段名称（中文）
    private String oldValue;
    private String newValue;
    
    // 申请状态
    private String status;
    private String statusName; // 状态名称（中文）
    private String reason;
    private String rejectReason;
    
    // 审核信息
    private Long reviewerId;
    private String reviewerName; // 审核人姓名（管理员姓名）
    private String reviewerUsername;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date reviewedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}


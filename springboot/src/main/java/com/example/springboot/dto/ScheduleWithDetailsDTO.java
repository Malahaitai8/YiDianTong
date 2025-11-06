package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 排班详细信息DTO（包含医生姓名、门诊名称等关联信息）
 */
@Data
public class ScheduleWithDetailsDTO {
    private Long id;
    private Long doctorId;
    private String doctorName;
    private Long departmentId;
    private String departmentName;
    private Long clinicId;
    private String clinicName;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date scheduleDate;
    private String timeSlot;
    private String slotType;
    private Integer totalSlots;
    private Integer availableSlots;
}
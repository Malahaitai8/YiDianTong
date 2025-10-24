package com.example.springboot.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Date;

/**
 * 更新排班请求DTO
 */
@Data
public class UpdateScheduleRequest {
    
    private Long doctorId;
    
    private Date scheduleDate;
    
    private String timeSlot; // morning/afternoon/evening
    
    private String slotType; // normal/expert/vip
    
    @Min(value = 0, message = "总号源数不能为负数")
    private Integer totalSlots;
    
    @Min(value = 0, message = "可用号源数不能为负数")
    private Integer availableSlots;
}


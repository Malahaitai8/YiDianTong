package com.example.springboot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 创建排班请求DTO
 */
@Data
public class CreateScheduleRequest {
    
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    @NotNull(message = "排班日期不能为空")
    private Date scheduleDate;
    
    @NotNull(message = "时间段不能为空")
    private String timeSlot; // morning/afternoon/evening
    
    @NotNull(message = "号别不能为空")
    private String slotType; // normal/expert/vip
    
    @NotNull(message = "总号源数不能为空")
    @Min(value = 1, message = "总号源数必须大于0")
    private Integer totalSlots;
    
    // 可用号源数，如果不传则默认等于总号源数
    private Integer availableSlots;
}


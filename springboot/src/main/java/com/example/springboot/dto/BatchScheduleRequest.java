package com.example.springboot.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 批量创建排班请求DTO
 */
@Data
public class BatchScheduleRequest {
    
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    @NotNull(message = "开始日期不能为空")
    private Date startDate;
    
    @NotNull(message = "结束日期不能为空")
    private Date endDate;
    
    @NotEmpty(message = "时间段列表不能为空")
    private List<String> timeSlots; // 例如: ["morning", "afternoon"]
    
    @NotNull(message = "号别不能为空")
    private String slotType; // normal/expert/vip
    
    @NotNull(message = "总号源数不能为空")
    @Min(value = 1, message = "总号源数必须大于0")
    private Integer totalSlots;
    
    // 是否跳过周末
    private Boolean skipWeekends = false;
    
    // 要排除的日期列表（例如节假日）
    private List<Date> excludeDates;
}


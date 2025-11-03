package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 更新排班规则请求
 */
@Data
public class UpdateScheduleRuleRequest {
    
    @Size(max = 100, message = "规则名称不能超过100字符")
    private String ruleName;
    
    @Pattern(regexp = "FIXED_WEEKLY|ROTATION|CUSTOM", message = "规则类型只能是 FIXED_WEEKLY, ROTATION, CUSTOM")
    private String ruleType;
    
    private Long doctorId;
    
    private Long departmentId;
    
    private Long clinicId;
    
    private List<Integer> weekDays;
    
    private List<String> timeSlots;
    
    @Pattern(regexp = "normal|expert|vip", message = "号别只能是 normal, expert, vip")
    private String slotType;
    
    @Min(value = 1, message = "总号源数至少为1")
    @Max(value = 100, message = "总号源数不能超过100")
    private Integer totalSlots;
    
    @Min(value = 1, message = "每天最多排班次数至少为1")
    private Integer maxDailySchedules;
    
    @Min(value = 1, message = "最多连续排班天数至少为1")
    private Integer maxContinuousDays;
    
    private Boolean skipWeekends;
    
    private Boolean skipHolidays;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    
    @Pattern(regexp = "ACTIVE|DISABLED", message = "状态只能是 ACTIVE, DISABLED")
    private String status;
    
    private Integer priority;
    
    @Size(max = 255, message = "描述不能超过255字符")
    private String description;
}


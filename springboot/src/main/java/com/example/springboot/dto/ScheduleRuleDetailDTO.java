package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 排班规则详情 DTO
 */
@Data
public class ScheduleRuleDetailDTO {
    
    private Long id;
    private String ruleName;
    private String ruleType;
    private String ruleTypeName; // 规则类型名称（中文）
    
    // 关联信息
    private Long doctorId;
    private String doctorName;
    private String doctorTitle;
    
    private Long departmentId;
    private String departmentName;
    
    private Long clinicId;
    private String clinicName;
    
    // 时间配置
    private String weekDays;
    private List<Integer> weekDaysList; // 星期列表
    private String weekDaysDisplay; // 星期显示（如：周一、周三、周五）
    
    private String timeSlots;
    private List<String> timeSlotsList; // 时段列表
    private String timeSlotsDisplay; // 时段显示（如：上午、下午）
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    
    // 号源配置
    private String slotType;
    private String slotTypeName; // 号别名称（中文）
    private Integer totalSlots;
    
    // 高级规则
    private Integer maxDailySchedules;
    private Integer maxContinuousDays;
    private Boolean skipWeekends;
    private Boolean skipHolidays;
    
    // 状态信息
    private String status;
    private String statusName; // 状态名称（中文）
    private Integer priority;
    private String description;
    
    // 审计信息
    private String createdBy;
    private String createdByName; // 创建人姓名
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
}


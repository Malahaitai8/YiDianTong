package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 创建排班规则请求
 */
@Data
public class CreateScheduleRuleRequest {
    
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称不能超过100字符")
    private String ruleName;
    
    @NotBlank(message = "规则类型不能为空")
    @Pattern(regexp = "FIXED_WEEKLY|ROTATION|CUSTOM", message = "规则类型只能是 FIXED_WEEKLY, ROTATION, CUSTOM")
    private String ruleType;
    
    /**
     * 医生ID（可选，不传表示通用规则）
     */
    private Long doctorId;
    
    /**
     * 科室ID（可选）
     */
    private Long departmentId;
    
    /**
     * 门诊ID（可选）
     */
    private Long clinicId;
    
    /**
     * 星期几（1-7）
     */
    private List<Integer> weekDays;
    
    /**
     * 时间段
     */
    @NotEmpty(message = "时间段不能为空")
    private List<String> timeSlots;
    
    /**
     * 号别
     */
    @NotBlank(message = "号别不能为空")
    @Pattern(regexp = "normal|expert|vip", message = "号别只能是 normal, expert, vip")
    private String slotType;
    
    /**
     * 默认总号源数
     */
    @NotNull(message = "总号源数不能为空")
    @Min(value = 1, message = "总号源数至少为1")
    @Max(value = 100, message = "总号源数不能超过100")
    private Integer totalSlots;
    
    /**
     * 每天最多排班次数
     */
    @Min(value = 1, message = "每天最多排班次数至少为1")
    private Integer maxDailySchedules;
    
    /**
     * 最多连续排班天数
     */
    @Min(value = 1, message = "最多连续排班天数至少为1")
    private Integer maxContinuousDays;
    
    /**
     * 是否跳过周末
     */
    private Boolean skipWeekends;
    
    /**
     * 是否跳过节假日
     */
    private Boolean skipHolidays;
    
    /**
     * 规则生效开始日期
     */
    @NotNull(message = "开始日期不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    
    /**
     * 规则生效结束日期（可选）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    
    /**
     * 优先级（可选，默认0）
     */
    private Integer priority;
    
    /**
     * 规则描述
     */
    @Size(max = 255, message = "描述不能超过255字符")
    private String description;
}


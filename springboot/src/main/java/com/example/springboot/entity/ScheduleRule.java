package com.example.springboot.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 排班规则实体类
 * 对应数据库表：schedule_rule
 */
@Data
public class ScheduleRule {
    
    /**
     * 规则唯一ID
     */
    private Long id;
    
    /**
     * 规则名称
     */
    private String ruleName;
    
    /**
     * 规则类型 (weekly/custom/template)
     */
    private String ruleType;
    
    /**
     * 关联医生ID (可为空表示科室规则)
     */
    private Long doctorId;
    
    /**
     * 关联科室ID
     */
    private Long departmentId;
    
    /**
     * 关联门诊ID
     */
    private Long clinicId;
    
    /**
     * 星期配置 (如: 1,3,5 表示周一三五)
     */
    private String weekDays;
    
    /**
     * 时段配置 (如: morning,afternoon)
     */
    private String timeSlots;
    
    /**
     * 规则生效开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date startDate;
    
    /**
     * 规则生效结束日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date endDate;
    
    /**
     * 号别类型 (normal/expert/vip)
     */
    private String slotType;
    
    /**
     * 每次排班的总号源数
     */
    private Integer totalSlots;
    
    /**
     * 每天最多排班次数
     */
    private Integer maxDailySchedules;
    
    /**
     * 最多连续出诊天数
     */
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
     * 规则状态 (ACTIVE/INACTIVE/EXPIRED)
     */
    private String status;
    
    /**
     * 优先级 (数字越大优先级越高)
     */
    private Integer priority;
    
    /**
     * 规则描述说明
     */
    private String description;
    
    /**
     * 创建人
     */
    private String createdBy;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;
    
    // 关联对象（用于返回详细信息）
    private Doctor doctor;
    private Department department;
    private Clinic clinic;
}


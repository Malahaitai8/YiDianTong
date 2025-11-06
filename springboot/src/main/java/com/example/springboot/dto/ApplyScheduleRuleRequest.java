package com.example.springboot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用排班规则请求
 * 根据规则自动生成排班
 */
@Data
public class ApplyScheduleRuleRequest {
    
    /**
     * 规则ID（必填）
     */
    @NotNull(message = "规则ID不能为空")
    private String ruleId;
    
    /**
     * 应用开始日期（可选，默认使用规则的开始日期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date applyStartDate;
    
    /**
     * 应用结束日期（可选，默认使用规则的结束日期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date applyEndDate;
    
    /**
     * 是否覆盖已存在的排班（默认false，跳过已存在的）
     */
    private Boolean overwriteExisting;
    
    /**
     * 排除的日期列表（可选，在此日期不生成排班）
     */
    private List<Date> excludeDates;
}


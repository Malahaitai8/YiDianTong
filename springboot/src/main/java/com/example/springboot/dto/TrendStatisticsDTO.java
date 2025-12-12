package com.example.springboot.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 趋势统计数据DTO
 */
@Data
public class TrendStatisticsDTO {
    private List<Map<String, Object>> appointmentTrend;  // 预约趋势（按日期）
    private List<Map<String, Object>> revenueTrend;       // 收入趋势（按日期）
    private List<Map<String, Object>> cancellationTrend; // 退号趋势（按日期）
    private String startDate;                              // 开始日期
    private String endDate;                               // 结束日期
}


























package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 收入统计数据DTO
 */
@Data
public class RevenueStatisticsDTO {
    private String date;                    // 日期
    private Long departmentId;              // 科室ID
    private String departmentName;          // 科室名称
    private String slotType;                // 号别类型
    private BigDecimal totalRevenue;       // 总收入
    private BigDecimal originalFee;        // 原始费用总和
    private BigDecimal actualFee;           // 实际费用总和
    private Integer appointmentCount;      // 预约数量
    private Double avgRevenuePerAppointment; // 平均每单收入
}

























































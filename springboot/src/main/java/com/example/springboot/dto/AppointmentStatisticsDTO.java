package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 预约统计数据DTO
 */
@Data
public class AppointmentStatisticsDTO {
    private String date;                    // 日期
    private String departmentName;          // 科室名称
    private Long departmentId;             // 科室ID
    private String doctorName;              // 医生姓名
    private Long doctorId;                  // 医生ID
    private Integer totalAppointments;     // 总预约数
    private Integer completedAppointments;  // 已完成数
    private Integer cancelledAppointments; // 已取消数
    private Integer noShowAppointments;    // 爽约数
    private Integer scheduledAppointments;  // 待就诊数
    private Double completionRate;         // 完成率
    private Double cancellationRate;      // 退号率
    private BigDecimal totalRevenue;       // 总收入
    private BigDecimal originalFee;        // 原始费用
    private BigDecimal actualFee;          // 实际费用
}






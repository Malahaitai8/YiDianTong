package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 医生工作量统计DTO
 */
@Data
public class DoctorWorkloadDTO {
    private Long doctorId;                  // 医生ID
    private String doctorName;             // 医生姓名
    private String doctorTitle;            // 医生职称
    private Long departmentId;             // 科室ID
    private String departmentName;         // 科室名称
    private Integer workDays;              // 工作天数
    private Integer totalAppointments;    // 总预约数
    private Integer completedAppointments; // 已完成数
    private Integer cancelledAppointments; // 已取消数
    private Integer noShowAppointments;    // 爽约数
    private Double avgPatientsPerDay;      // 日均接诊数
    private BigDecimal totalRevenue;      // 总收入
    private Double completionRate;         // 完成率
    private Double cancellationRate;       // 退号率
}




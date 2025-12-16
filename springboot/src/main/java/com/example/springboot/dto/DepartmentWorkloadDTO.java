package com.example.springboot.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 科室负荷统计DTO
 */
@Data
public class DepartmentWorkloadDTO {
    private Long departmentId;              // 科室ID
    private String departmentName;          // 科室名称
    private Integer doctorCount;           // 医生数量
    private Integer totalAppointments;    // 总预约数
    private Integer completedAppointments; // 已完成数
    private Integer cancelledAppointments; // 已取消数
    private Double completionRate;         // 完成率
    private Double cancellationRate;       // 退号率
    private BigDecimal totalRevenue;      // 总收入
    private Integer totalSlots;            // 总号源数
    private Integer usedSlots;             // 已使用号源数
    private Double utilizationRate;        // 号源利用率
}








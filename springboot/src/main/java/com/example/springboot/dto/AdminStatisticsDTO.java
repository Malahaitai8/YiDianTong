package com.example.springboot.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 管理端统计报表DTO
 */
@Data
public class AdminStatisticsDTO {
    // 总体数据
    private Integer totalAppointments;       // 总预约数
    private Integer totalPatients;           // 总患者数
    private Integer totalDoctors;            // 总医生数
    private Integer totalDepartments;        // 总科室数
    
    // 今日数据
    private Integer todayAppointments;       // 今日预约数
    private Integer todayCompleted;          // 今日完成数
    private Integer todayCancelled;          // 今日取消数
    private Integer todayPending;            // 今日待就诊数
    
    // 退号分析
    private Integer totalCancellations;      // 总取消数
    private Double cancellationRate;         // 退号率
    
    // 候补数据
    private Integer totalWaitlist;           // 总候补数
    private Integer waitlistPending;         // 待候补数
    private Integer waitlistNotified;        // 已通知数
    
    // 科室负荷（按科室统计预约数）
    private List<Map<String, Object>> departmentWorkload;
    
    // 医生工作量（按医生统计预约数）
    private List<Map<String, Object>> doctorWorkload;
    
    // 时段分布
    private Map<String, Integer> timeSlotDistribution;
    
    // 号别分布
    private Map<String, Integer> slotTypeDistribution;
    
    // 趋势数据（最近7天/30天）
    private List<Map<String, Object>> appointmentTrend;
}


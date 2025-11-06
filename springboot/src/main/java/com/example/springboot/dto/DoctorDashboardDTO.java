package com.example.springboot.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 医生Dashboard数据DTO
 */
@Data
public class DoctorDashboardDTO {
    // 今日数据
    private Integer todayAppointments;       // 今日预约总数
    private Integer todayPending;            // 今日待就诊数
    private Integer todayCompleted;          // 今日已完成数
    private Integer todayCancelled;          // 今日已取消数
    
    // 本周数据
    private Integer weekAppointments;        // 本周预约总数
    private Integer weekSchedules;           // 本周排班次数
    
    // 申请状态
    private Integer pendingChangeRequests;   // 待审核调班申请数
    private Integer pendingInfoRequests;     // 待审核信息修改申请数
    
    // 近期排班
    private List<DoctorScheduleDTO> upcomingSchedules;  // 近7天排班
    
    // 今日患者列表
    private List<DoctorPatientDTO> todayPatients;       // 今日患者
    
    // 统计数据
    private Map<String, Object> statistics;  // 其他统计数据
}


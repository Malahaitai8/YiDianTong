package com.example.springboot.service;

import com.example.springboot.dto.OverviewStatsDTO;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StatisticsService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    public OverviewStatsDTO getOverviewStats() {
        OverviewStatsDTO stats = new OverviewStatsDTO();

        // 1. 预约状态统计
        int totalAppointments = appointmentMapper.countTotal();
        int completedAppointments = appointmentMapper.countByStatus("COMPLETED");
        int cancelledAppointments = appointmentMapper.countByStatus("CANCELLED");
        int noShowAppointments = appointmentMapper.countByStatus("NO_SHOW");

        stats.setTotalAppointments(totalAppointments);
        stats.setCompletedAppointments(completedAppointments);
        stats.setCancelledAppointments(cancelledAppointments);
        stats.setNoShowAppointments(noShowAppointments);

        // 2. 号源统计
        Integer totalSlots = scheduleMapper.sumTotalSlots();
        Integer availableSlots = scheduleMapper.sumAvailableSlots();
        totalSlots = (totalSlots == null) ? 0 : totalSlots;
        availableSlots = (availableSlots == null) ? 0 : availableSlots;
        int usedSlots = totalSlots - availableSlots;

        stats.setTotalSlots(totalSlots);
        stats.setAvailableSlots(availableSlots);
        stats.setUsedSlots(usedSlots);

        // 3. 计算衍生指标
        // 就诊完成率 = 已完成 / (总数 - 已取消)
        double completionRate = 0;
        if (totalAppointments - cancelledAppointments > 0) {
            completionRate = (double) completedAppointments / (totalAppointments - cancelledAppointments);
        }
        stats.setCompletionRate(round(completionRate, 4));

        // 号源利用率 = 已使用号源 / 总号源
        double utilization = 0;
        if (totalSlots > 0) {
            utilization = (double) usedSlots / totalSlots;
        }
        stats.setUtilization(round(utilization, 4));

        return stats;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}


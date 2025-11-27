package com.example.springboot.service;

import com.example.springboot.dto.OverviewStatsDTO;
import com.example.springboot.dto.DailyAppointmentStatsDTO;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    /**
     * 获取最近若干天的按天预约统计（默认7天），可按科室过滤
     */
    public List<DailyAppointmentStatsDTO> getWeeklyAppointmentStats(Integer days, Long departmentId) {
        if (days == null || days <= 0) {
            days = 7;
        }
        LocalDate today = LocalDate.now();
        LocalDate startDateLocal = today.minusDays(days - 1);
        LocalDate endDateLocal = today.plusDays(1); // 结束为次日0点（开区间）

        Date startDate = Date.from(startDateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(endDateLocal.atStartOfDay(ZoneId.systemDefault()).toInstant());

        List<DailyAppointmentStatsDTO> rawList =
                appointmentMapper.selectDailyAppointmentStats(startDate, endDate, departmentId);

        // 补齐日期，保证连续性
        List<DailyAppointmentStatsDTO> filled = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            LocalDate day = startDateLocal.plusDays(i);
            String dateStr = day.toString(); // yyyy-MM-dd
            DailyAppointmentStatsDTO found = null;
            if (rawList != null) {
                for (DailyAppointmentStatsDTO dto : rawList) {
                    if (dateStr.equals(dto.getDate())) {
                        found = dto;
                        break;
                    }
                }
            }
            if (found == null) {
                found = new DailyAppointmentStatsDTO();
                found.setDate(dateStr);
            }
            filled.add(found);
        }
        return filled;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}


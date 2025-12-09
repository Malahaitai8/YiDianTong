package com.example.springboot.service;

import com.example.springboot.dto.*;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.mapper.DepartmentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    public OverviewStatsDTO getOverviewStats(Long departmentId, Date startDate, Date endDate) {
        OverviewStatsDTO stats = new OverviewStatsDTO();

        // 1. 预约状态统计
        int totalAppointments = appointmentMapper.countTotal(departmentId, startDate, endDate);
        int completedAppointments = appointmentMapper.countByStatus("COMPLETED", departmentId, startDate, endDate);
        int cancelledAppointments = appointmentMapper.countByStatus("CANCELLED", departmentId, startDate, endDate);
        int noShowAppointments = appointmentMapper.countByStatus("NO_SHOW", departmentId, startDate, endDate);

        stats.setTotalAppointments(totalAppointments);
        stats.setCompletedAppointments(completedAppointments);
        stats.setCancelledAppointments(cancelledAppointments);
        stats.setNoShowAppointments(noShowAppointments);

        // 2. 号源统计（基于排班日期）
        Integer totalSlots = scheduleMapper.sumTotalSlots(departmentId, startDate, endDate);
        Integer availableSlots = scheduleMapper.sumAvailableSlots(departmentId, startDate, endDate);
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

    /**
     * 获取预约统计（按日期范围）
     */
    public List<AppointmentStatisticsDTO> getAppointmentStatistics(Date startDate, Date endDate) {
        List<Map<String, Object>> data = appointmentMapper.countByDateRange(startDate, endDate);
        return convertToAppointmentStatistics(data);
    }

    /**
     * 获取科室负荷统计
     */
    public List<DepartmentWorkloadDTO> getDepartmentWorkload(Date startDate, Date endDate) {
        List<Map<String, Object>> data = appointmentMapper.countByDepartment(startDate, endDate);
        return convertToDepartmentWorkload(data);
    }

    /**
     * 获取医生工作量统计
     */
    public List<DoctorWorkloadDTO> getDoctorWorkload(Date startDate, Date endDate, Long doctorId) {
        List<Map<String, Object>> data = appointmentMapper.countByDoctor(startDate, endDate, doctorId);
        return convertToDoctorWorkload(data);
    }

    /**
     * 获取收入统计（按日期范围）
     */
    public List<RevenueStatisticsDTO> getRevenueStatistics(Date startDate, Date endDate) {
        List<Map<String, Object>> data = appointmentMapper.sumRevenueByDateRange(startDate, endDate, null);
        return convertToRevenueStatistics(data);
    }

    /**
     * 获取收入统计（按科室）
     */
    public List<RevenueStatisticsDTO> getRevenueByDepartment(Date startDate, Date endDate) {
        List<Map<String, Object>> data = appointmentMapper.sumRevenueByDepartment(startDate, endDate);
        return convertToRevenueStatistics(data);
    }

    /**
     * 获取号别分布统计
     */
    public List<Map<String, Object>> getSlotTypeDistribution(Date startDate, Date endDate, Long departmentId) {
        return appointmentMapper.countBySlotType(startDate, endDate, departmentId);
    }

    /**
     * 获取时间段分布统计
     */
    public List<Map<String, Object>> getTimeSlotDistribution(Date startDate, Date endDate, Long departmentId) {
        return appointmentMapper.countByTimeSlot(startDate, endDate, departmentId);
    }

    /**
     * 获取退号率统计（按科室）
     */
    public List<Map<String, Object>> getCancellationRateByDepartment(Date startDate, Date endDate) {
        return appointmentMapper.getCancellationRateByDepartment(startDate, endDate);
    }

    /**
     * 获取退号率统计（按医生）
     */
    public List<Map<String, Object>> getCancellationRateByDoctor(Date startDate, Date endDate, Long doctorId) {
        return appointmentMapper.getCancellationRateByDoctor(startDate, endDate, doctorId);
    }

    /**
     * 获取预约趋势统计
     */
    public TrendStatisticsDTO getTrendStatistics(Date startDate, Date endDate, Long departmentId) {
        TrendStatisticsDTO trend = new TrendStatisticsDTO();
        
        // 预约趋势
        List<Map<String, Object>> appointmentTrend = appointmentMapper.getAppointmentTrend(startDate, endDate, departmentId);
        trend.setAppointmentTrend(appointmentTrend);
        
        // 收入趋势
        List<Map<String, Object>> revenueTrend = appointmentMapper.sumRevenueByDateRange(startDate, endDate, departmentId);
        trend.setRevenueTrend(revenueTrend);
        
        // 退号趋势（从预约趋势中提取）
        List<Map<String, Object>> cancellationTrend = new ArrayList<>();
        for (Map<String, Object> item : appointmentTrend) {
            Map<String, Object> cancelItem = new HashMap<>();
            cancelItem.put("date", item.get("date"));
            cancelItem.put("cancelledCount", item.get("cancelledCount"));
            cancelItem.put("cancellationRate", item.get("cancellationRate"));
            cancellationTrend.add(cancelItem);
        }
        trend.setCancellationTrend(cancellationTrend);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        trend.setStartDate(sdf.format(startDate));
        trend.setEndDate(sdf.format(endDate));
        
        return trend;
    }

    // ========== 数据转换方法 ==========

    private List<AppointmentStatisticsDTO> convertToAppointmentStatistics(List<Map<String, Object>> data) {
        return data.stream().map(item -> {
            AppointmentStatisticsDTO dto = new AppointmentStatisticsDTO();
            dto.setDate(getStringValue(item, "date"));
            dto.setDepartmentName(getStringValue(item, "departmentName"));
            dto.setDepartmentId(getLongValue(item, "departmentId"));
            dto.setDoctorName(getStringValue(item, "doctorName"));
            dto.setDoctorId(getLongValue(item, "doctorId"));
            dto.setTotalAppointments(getIntValue(item, "totalCount"));
            dto.setCompletedAppointments(getIntValue(item, "completedCount"));
            dto.setCancelledAppointments(getIntValue(item, "cancelledCount"));
            dto.setNoShowAppointments(getIntValue(item, "noShowCount"));
            dto.setScheduledAppointments(getIntValue(item, "scheduledCount"));
            
            // 计算完成率和退号率
            int total = dto.getTotalAppointments();
            int cancelled = dto.getCancelledAppointments();
            int completed = dto.getCompletedAppointments();
            
            if (total > 0) {
                dto.setCancellationRate(round((double) cancelled / total, 4));
            }
            if (total - cancelled > 0) {
                dto.setCompletionRate(round((double) completed / (total - cancelled), 4));
            }
            
            dto.setTotalRevenue(getBigDecimalValue(item, "totalRevenue"));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<DepartmentWorkloadDTO> convertToDepartmentWorkload(List<Map<String, Object>> data) {
        return data.stream().map(item -> {
            DepartmentWorkloadDTO dto = new DepartmentWorkloadDTO();
            dto.setDepartmentId(getLongValue(item, "departmentId"));
            dto.setDepartmentName(getStringValue(item, "departmentName"));
            dto.setDoctorCount(getIntValue(item, "doctorCount"));
            dto.setTotalAppointments(getIntValue(item, "totalCount"));
            dto.setCompletedAppointments(getIntValue(item, "completedCount"));
            dto.setCancelledAppointments(getIntValue(item, "cancelledCount"));
            
            int total = dto.getTotalAppointments();
            int cancelled = dto.getCancelledAppointments();
            int completed = dto.getCompletedAppointments();
            
            if (total > 0) {
                dto.setCancellationRate(round((double) cancelled / total, 4));
            }
            if (total - cancelled > 0) {
                dto.setCompletionRate(round((double) completed / (total - cancelled), 4));
            }
            
            dto.setTotalRevenue(getBigDecimalValue(item, "totalRevenue"));
            dto.setTotalSlots(getIntValue(item, "totalSlots"));
            dto.setUsedSlots(getIntValue(item, "usedSlots"));
            
            int totalSlots = dto.getTotalSlots();
            if (totalSlots > 0) {
                dto.setUtilizationRate(round((double) dto.getUsedSlots() / totalSlots, 4));
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    private List<DoctorWorkloadDTO> convertToDoctorWorkload(List<Map<String, Object>> data) {
        return data.stream().map(item -> {
            DoctorWorkloadDTO dto = new DoctorWorkloadDTO();
            dto.setDoctorId(getLongValue(item, "doctorId"));
            dto.setDoctorName(getStringValue(item, "doctorName"));
            dto.setDoctorTitle(getStringValue(item, "doctorTitle"));
            dto.setDepartmentId(getLongValue(item, "departmentId"));
            dto.setDepartmentName(getStringValue(item, "departmentName"));
            dto.setWorkDays(getIntValue(item, "workDays"));
            dto.setTotalAppointments(getIntValue(item, "totalCount"));
            dto.setCompletedAppointments(getIntValue(item, "completedCount"));
            dto.setCancelledAppointments(getIntValue(item, "cancelledCount"));
            dto.setNoShowAppointments(getIntValue(item, "noShowCount"));
            
            int workDays = dto.getWorkDays();
            int total = dto.getTotalAppointments();
            if (workDays > 0) {
                dto.setAvgPatientsPerDay(round((double) total / workDays, 2));
            }
            
            int cancelled = dto.getCancelledAppointments();
            int completed = dto.getCompletedAppointments();
            
            if (total > 0) {
                dto.setCancellationRate(round((double) cancelled / total, 4));
            }
            if (total - cancelled > 0) {
                dto.setCompletionRate(round((double) completed / (total - cancelled), 4));
            }
            
            dto.setTotalRevenue(getBigDecimalValue(item, "totalRevenue"));
            return dto;
        }).collect(Collectors.toList());
    }

    private List<RevenueStatisticsDTO> convertToRevenueStatistics(List<Map<String, Object>> data) {
        return data.stream().map(item -> {
            RevenueStatisticsDTO dto = new RevenueStatisticsDTO();
            dto.setDate(getStringValue(item, "date"));
            dto.setDepartmentId(getLongValue(item, "departmentId"));
            dto.setDepartmentName(getStringValue(item, "departmentName"));
            dto.setSlotType(getStringValue(item, "slotType"));
            dto.setTotalRevenue(getBigDecimalValue(item, "totalRevenue"));
            dto.setOriginalFee(getBigDecimalValue(item, "originalFee"));
            dto.setActualFee(getBigDecimalValue(item, "actualFee"));
            dto.setAppointmentCount(getIntValue(item, "appointmentCount"));
            
            int count = dto.getAppointmentCount();
            BigDecimal revenue = dto.getTotalRevenue();
            if (count > 0 && revenue != null) {
                dto.setAvgRevenuePerAppointment(revenue.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP).doubleValue());
            }
            
            return dto;
        }).collect(Collectors.toList());
    }

    // ========== 工具方法 ==========

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    private Integer getIntValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }
}


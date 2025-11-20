package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Appointment;
import com.example.springboot.entity.Doctor;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Schedule;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.AppointmentMapper;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.mapper.PatientMapper;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DoctorService {

    private static final Logger logger = LoggerFactory.getLogger(DoctorService.class);

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private AppointmentMapper appointmentMapper;

    @Resource
    private PatientMapper patientMapper;

    public List<Doctor> selectAll() {
        List<Doctor> list = doctorMapper.selectAll();
        markTodayDuty(list);
        return list;
    }

    public Doctor selectById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        if (doctor != null && doctor.getId() != null) {
            doctor.setOnDutyToday(isDoctorOnDutyToday(doctor.getId()));
        }
        return doctor;
    }

    public int create(Doctor doctor) {
        return doctorMapper.insert(doctor);
    }

    /**
     * 更新医生信息
     * 权限检查：管理员可以更新任何医生，医生只能更新自己的信息
     */
    public int update(Doctor doctor) {
        // 检查医生是否存在
        Doctor existingDoctor = doctorMapper.selectById(doctor.getId());
        if (existingDoctor == null) {
            throw new CustomerException("医生不存在");
        }

        // 如果当前用户不是管理员，则检查是否为本人操作
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new CustomerException("401", "未登录");
            }
            
            // 检查是否为本人
            if (!currentUserId.equals(existingDoctor.getUserId())) {
                logger.warn("用户 {} 尝试修改医生 {} 的信息，但该医生关联的用户ID为 {}", 
                           currentUserId, doctor.getId(), existingDoctor.getUserId());
                throw new CustomerException("403", "无权限修改其他医生的信息");
            }
        }

        return doctorMapper.update(doctor);
    }

    public int delete(Long id) {
        return doctorMapper.delete(id);
    }

    private boolean isDoctorOnDutyToday(Long doctorId) {
        if (doctorId == null) {
            return false;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startOfDay = calendar.getTime();

        calendar.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = calendar.getTime();

        int count = scheduleMapper.countByDoctorAndDateRange(doctorId, startOfDay, endOfDay);
        return count > 0;
    }

    private void markTodayDuty(List<Doctor> doctors) {
        if (doctors == null || doctors.isEmpty()) {
            return;
        }
        for (Doctor doctor : doctors) {
            if (doctor == null) {
                continue;
            }
            doctor.setOnDutyToday(isDoctorOnDutyToday(doctor.getId()));
        }
    }

    // ==================== 医生端功能 ====================

    /**
     * 医生查看自己的排班
     */
    public Map<String, Object> getMySchedules(Long doctorId, Date startDate, Date endDate, String timeSlot) {
        // 规范化开始/结束日期，并确保结束日期为“次日零点”（用于 < endDate 的区间查询）
        // 如果未指定日期范围，默认查询从今天开始的未来30天（含最后一天）
        Calendar cal = Calendar.getInstance();
        if (startDate == null) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
        } else {
            cal.setTime(startDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            startDate = cal.getTime();
        }

        if (endDate == null) {
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(startDate);
            endCal.add(Calendar.DAY_OF_MONTH, 30); // 默认30天区间
            // 设为次日零点以便使用 < endDate 包含最后一天
            endCal.add(Calendar.DAY_OF_MONTH, 1);
            endCal.set(Calendar.HOUR_OF_DAY, 0);
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);
            endDate = endCal.getTime();
        } else {
            Calendar endCal = Calendar.getInstance();
            endCal.setTime(endDate);
            // 用户传入的 endDate 通常为当天零点；将其规范为“次日零点”，以包含传入当日
            endCal.add(Calendar.DAY_OF_MONTH, 1);
            endCal.set(Calendar.HOUR_OF_DAY, 0);
            endCal.set(Calendar.MINUTE, 0);
            endCal.set(Calendar.SECOND, 0);
            endCal.set(Calendar.MILLISECOND, 0);
            endDate = endCal.getTime();
        }

        // 查询排班
        List<Schedule> schedules = scheduleMapper.selectByDoctorAndDateRange(
            doctorId, startDate, endDate
        );

        // 如果指定了时间段，进行筛选（兼容大小写与中文传参）
        if (timeSlot != null && !timeSlot.isEmpty()) {
            String normalized = normalizeTimeSlot(timeSlot);
            schedules.removeIf(s -> {
                String slot = s.getTimeSlot();
                return slot == null || !normalized.equalsIgnoreCase(slot);
            });
        }

        // 为每个排班统计预约信息
        List<Map<String, Object>> enrichedSchedules = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Schedule schedule : schedules) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", schedule.getId());
            item.put("scheduleDate", sdf.format(schedule.getScheduleDate()));
            item.put("timeSlot", schedule.getTimeSlot());
            item.put("slotType", schedule.getSlotType());
            item.put("totalSlots", schedule.getTotalSlots());
            item.put("availableSlots", schedule.getAvailableSlots());
            item.put("bookedSlots", schedule.getTotalSlots() - schedule.getAvailableSlots());
            
            // 查询该排班的预约数量
            int appointmentCount = appointmentMapper.countByScheduleId(schedule.getId());
            item.put("appointmentCount", appointmentCount);
            
            enrichedSchedules.add(item);
        }

        // 按日期和时间段排序
        enrichedSchedules.sort((a, b) -> {
            int dateCompare = ((String) a.get("scheduleDate")).compareTo((String) b.get("scheduleDate"));
            if (dateCompare != 0) return dateCompare;
            return getTimeSlotOrder((String) a.get("timeSlot")) - getTimeSlotOrder((String) b.get("timeSlot"));
        });

        Map<String, Object> result = new HashMap<>();
        result.put("schedules", enrichedSchedules);
        result.put("total", enrichedSchedules.size());
        
        return result;
    }

    /**
     * 医生查看预约患者列表
     */
    public Map<String, Object> getMyPatients(Long doctorId, Date date, String timeSlot, 
                                             String status, String patientName) {
        // 查询该医生的所有预约
        List<Appointment> appointments = appointmentMapper.selectByDoctorId(doctorId);

        // 筛选条件
        List<Map<String, Object>> filteredPatients = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Appointment appointment : appointments) {
            // 日期筛选
            if (date != null && appointment.getAppointmentTime() != null) {
                Calendar appointmentCal = Calendar.getInstance();
                appointmentCal.setTime(appointment.getAppointmentTime());
                Calendar filterCal = Calendar.getInstance();
                filterCal.setTime(date);
                
                if (appointmentCal.get(Calendar.YEAR) != filterCal.get(Calendar.YEAR) ||
                    appointmentCal.get(Calendar.DAY_OF_YEAR) != filterCal.get(Calendar.DAY_OF_YEAR)) {
                    continue;
                }
            }

            // 状态筛选
            if (status != null && !status.isEmpty() && !status.equals(appointment.getStatus())) {
                continue;
            }

            // 获取排班信息（用于时间段筛选）
            Schedule schedule = scheduleMapper.selectById(appointment.getScheduleId());
            if (timeSlot != null && !timeSlot.isEmpty()) {
                String normalized = normalizeTimeSlot(timeSlot);
                if (schedule == null || schedule.getTimeSlot() == null ||
                    !normalized.equalsIgnoreCase(schedule.getTimeSlot())) {
                    continue;
                }
            }

            // 获取患者信息
            Patient patient = patientMapper.selectById(appointment.getPatientId());
            if (patient == null) {
                continue;
            }

            // 患者姓名筛选
            if (patientName != null && !patientName.isEmpty()) {
                if (patient.getName() == null || !patient.getName().contains(patientName)) {
                    continue;
                }
            }

            // 构建返回数据
            Map<String, Object> item = new HashMap<>();
            item.put("appointmentId", appointment.getId());
            item.put("appointmentTime", appointment.getAppointmentTime() != null ? 
                     sdfTime.format(appointment.getAppointmentTime()) : null);
            item.put("status", appointment.getStatus());
            item.put("sourceType", appointment.getSourceType());
            
            // 患者信息
            item.put("patientId", patient.getId());
            item.put("patientName", patient.getName());
            item.put("patientGender", null);  // User实体中无性别字段
            item.put("patientAge", null);     // User实体中无年龄字段
            item.put("patientPhone", patient.getPhoneNumber());
            item.put("patientIdCard", patient.getIdCardNumber());
            
            // 排班信息
            if (schedule != null) {
                item.put("scheduleDate", sdf.format(schedule.getScheduleDate()));
                item.put("timeSlot", schedule.getTimeSlot());
                item.put("slotType", schedule.getSlotType());
            }
            
            filteredPatients.add(item);
        }

        // 按预约时间倒序排序（最新的在前）
        filteredPatients.sort((a, b) -> {
            String timeA = (String) a.get("appointmentTime");
            String timeB = (String) b.get("appointmentTime");
            if (timeA == null) return 1;
            if (timeB == null) return -1;
            return timeB.compareTo(timeA);
        });

        Map<String, Object> result = new HashMap<>();
        result.put("patients", filteredPatients);
        result.put("total", filteredPatients.size());
        
        return result;
    }

    /**
     * 医生Dashboard
     */
    public Map<String, Object> getDoctorDashboard(Long doctorId) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // 1. 今日待接诊人数
        int todayPatients = countTodayPatients(doctorId);
        dashboard.put("todayPatients", todayPatients);
        
        // 2. 本周排班数量
        int weekSchedules = countWeekSchedules(doctorId);
        dashboard.put("weekSchedules", weekSchedules);
        
        // 3. 待审核的调班申请数量
        dashboard.put("pendingChangeRequests", 0);
        
        // 4. 近7天统计
        Map<String, Integer> last7Days = getLast7DaysStatistics(doctorId);
        dashboard.put("last7DaysAppointments", last7Days);
        
        // 5. 本月累计接诊人数
        int monthlyTotal = countMonthlyAppointments(doctorId);
        dashboard.put("monthlyTotal", monthlyTotal);
        
        // 6. 最近的排班（未来3天）
        List<Map<String, Object>> upcomingSchedules = getUpcomingSchedules(doctorId, 3);
        dashboard.put("upcomingSchedules", upcomingSchedules);
        
        return dashboard;
    }

    // ==================== 私有辅助方法 ====================

    private int countTodayPatients(Long doctorId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startOfDay = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        Date endOfDay = cal.getTime();
        
        List<Appointment> appointments = appointmentMapper.selectByDoctorId(doctorId);
        int count = 0;
        
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentTime() != null &&
                !appointment.getAppointmentTime().before(startOfDay) &&
                appointment.getAppointmentTime().before(endOfDay) &&
                ("scheduled".equals(appointment.getStatus()))) {
                count++;
            }
        }
        
        return count;
    }

    private int countWeekSchedules(Long doctorId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, 7);
        Date endDate = cal.getTime();
        
        List<Schedule> schedules = scheduleMapper.selectByDoctorAndDateRange(doctorId, startDate, endDate);
        return schedules.size();
    }

    private Map<String, Integer> getLast7DaysStatistics(Long doctorId) {
        Map<String, Integer> stats = new LinkedHashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -6);
        
        for (int i = 0; i < 7; i++) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date dayStart = cal.getTime();
            
            cal.add(Calendar.DAY_OF_MONTH, 1);
            Date dayEnd = cal.getTime();
            
            int count = countAppointmentsInRange(doctorId, dayStart, dayEnd);
            stats.put(sdf.format(dayStart), count);
        }
        
        return stats;
    }

    private int countMonthlyAppointments(Long doctorId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date monthStart = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        Date monthEnd = cal.getTime();
        
        return countAppointmentsInRange(doctorId, monthStart, monthEnd);
    }

    private int countAppointmentsInRange(Long doctorId, Date startDate, Date endDate) {
        List<Appointment> appointments = appointmentMapper.selectByDoctorId(doctorId);
        int count = 0;
        
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentTime() != null &&
                !appointment.getAppointmentTime().before(startDate) &&
                appointment.getAppointmentTime().before(endDate)) {
                count++;
            }
        }
        
        return count;
    }

    private List<Map<String, Object>> getUpcomingSchedules(Long doctorId, int days) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.DAY_OF_MONTH, days);
        Date endDate = cal.getTime();
        
        List<Schedule> schedules = scheduleMapper.selectByDoctorAndDateRange(doctorId, startDate, endDate);
        List<Map<String, Object>> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        for (Schedule schedule : schedules) {
            Map<String, Object> item = new HashMap<>();
            item.put("scheduleDate", sdf.format(schedule.getScheduleDate()));
            item.put("timeSlot", schedule.getTimeSlot());
            item.put("slotType", schedule.getSlotType());
            item.put("totalSlots", schedule.getTotalSlots());
            item.put("availableSlots", schedule.getAvailableSlots());
            result.add(item);
        }
        
        return result;
    }

    private int getTimeSlotOrder(String timeSlot) {
        if (timeSlot == null) return 99;
        String t = timeSlot.trim().toUpperCase();
        switch (t) {
            case "MORNING": return 1;
            case "AFTERNOON": return 2;
            case "EVENING": return 3;
            case "上午": return 1;
            case "下午": return 2;
            case "晚上": return 3;
            default:
                // 兼容数据库 lowercase
                if ("morning".equalsIgnoreCase(timeSlot)) return 1;
                if ("afternoon".equalsIgnoreCase(timeSlot)) return 2;
                if ("evening".equalsIgnoreCase(timeSlot)) return 3;
                return 99;
        }
    }

    private String normalizeTimeSlot(String timeSlot) {
        if (timeSlot == null) return null;
        String t = timeSlot.trim().toUpperCase();
        switch (t) {
            case "MORNING":
            case "上午":
                return "morning";
            case "AFTERNOON":
            case "下午":
                return "afternoon";
            case "EVENING":
            case "晚上":
                return "evening";
            default:
                return timeSlot.toLowerCase();
        }
    }
}

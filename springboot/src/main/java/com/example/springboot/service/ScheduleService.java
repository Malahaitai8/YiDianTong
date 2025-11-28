package com.example.springboot.service;

import com.example.springboot.dto.*;
import com.example.springboot.entity.Schedule;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.DoctorMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ScheduleService {

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorMapper doctorMapper;


    @Resource
    private AppointmentService appointmentService;

    @Resource
    private SystemConfigService systemConfigService;

    private final String[] DAYS_OF_WEEK = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    public List<ScheduleDTO> getWeekSchedule() {
        // 获取当前日期
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date startDate = calendar.getTime();

        // 获取一周后的日期
        calendar.add(Calendar.DAY_OF_MONTH, 7);
        Date endDate = calendar.getTime();

        // 查询数据库
        List<Schedule> schedules = scheduleMapper.selectSchedulesByDateRange(startDate, endDate);

        // 转换为DTO并添加星期几信息
        List<ScheduleDTO> scheduleDTOs = new ArrayList<>();
        for (Schedule schedule : schedules) {
            ScheduleDTO dto = new ScheduleDTO();
            BeanUtils.copyProperties(schedule, dto);

            // 获取星期几
            calendar.setTime(schedule.getScheduleDate());
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            dto.setDayOfWeek(DAYS_OF_WEEK[dayOfWeek]);

            scheduleDTOs.add(dto);
        }

        return scheduleDTOs;
    }

    // ========== 管理端排班管理方法 ==========

    /**
     * 创建单个排班
     */
    @Transactional
    public Schedule createSchedule(CreateScheduleRequest request) {
        // 1. 验证医生是否存在
        if (doctorMapper.selectById(request.getDoctorId()) == null) {
            throw new RuntimeException("医生不存在");
        }

        // 2. 检查是否已存在相同的排班
        int exists = scheduleMapper.checkScheduleExists(
            request.getDoctorId(),
            request.getScheduleDate(),
            request.getTimeSlot()
        );
        if (exists > 0) {
            throw new RuntimeException("该时间段已存在排班，请勿重复创建");
        }

        // 3. 创建排班记录
        Schedule schedule = new Schedule();
        schedule.setDoctorId(request.getDoctorId());
        schedule.setScheduleDate(request.getScheduleDate());
        schedule.setTimeSlot(request.getTimeSlot());
        schedule.setSlotType(request.getSlotType());
        schedule.setTotalSlots(request.getTotalSlots());

        // 如果没有指定可用号源数，则默认等于总号源数
        if (request.getAvailableSlots() != null) {
            schedule.setAvailableSlots(request.getAvailableSlots());
        } else {
            schedule.setAvailableSlots(request.getTotalSlots());
        }

        scheduleMapper.insert(schedule);
        return schedule;
    }

    /**
     * 批量创建排班
     */
    @Transactional
    public Map<String, Object> batchCreateSchedule(BatchScheduleRequest request) {
        // 1. 验证医生是否存在
        if (doctorMapper.selectById(request.getDoctorId()) == null) {
            throw new RuntimeException("医生不存在");
        }

        // 2. 验证日期范围
        if (request.getEndDate().before(request.getStartDate())) {
            throw new RuntimeException("结束日期不能早于开始日期");
        }

        // 3. 生成日期列表
        List<Date> dateList = generateDateList(
            request.getStartDate(),
            request.getEndDate(),
            request.getSkipWeekends(),
            request.getExcludeDates()
        );

        // 4. 批量创建排班
        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        for (Date date : dateList) {
            for (String timeSlot : request.getTimeSlots()) {
                try {
                    // 检查是否已存在
                    int exists = scheduleMapper.checkScheduleExists(
                        request.getDoctorId(), date, timeSlot
                    );
                    if (exists > 0) {
                        skipCount++;
                        continue;
                    }

                    // 创建排班
                    Schedule schedule = new Schedule();
                    schedule.setDoctorId(request.getDoctorId());
                    schedule.setScheduleDate(date);
                    schedule.setTimeSlot(timeSlot);
                    schedule.setSlotType(request.getSlotType());
                    schedule.setTotalSlots(request.getTotalSlots());
                    schedule.setAvailableSlots(request.getTotalSlots());

                    scheduleMapper.insert(schedule);
                    successCount++;
                } catch (Exception e) {
                    errors.add(String.format("日期 %s 时段 %s 创建失败: %s", date, timeSlot, e.getMessage()));
                }
            }
        }

        // 5. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        result.put("message", String.format("成功创建 %d 个排班，跳过 %d 个已存在的排班", successCount, skipCount));

        return result;
    }

    /**
     * 更新排班
     */
    @Transactional
    public Schedule updateSchedule(Long id, UpdateScheduleRequest request) {
        // 1. 查询原排班
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 2. 更新字段
        if (request.getDoctorId() != null) {
            if (doctorMapper.selectById(request.getDoctorId()) == null) {
                throw new RuntimeException("医生不存在");
            }
            schedule.setDoctorId(request.getDoctorId());
        }
        if (request.getScheduleDate() != null) {
            schedule.setScheduleDate(request.getScheduleDate());
        }
        if (request.getTimeSlot() != null) {
            schedule.setTimeSlot(request.getTimeSlot());
        }
        if (request.getSlotType() != null) {
            schedule.setSlotType(request.getSlotType());
        }
        if (request.getTotalSlots() != null) {
            schedule.setTotalSlots(request.getTotalSlots());
        }
        if (request.getAvailableSlots() != null) {
            schedule.setAvailableSlots(request.getAvailableSlots());
        }

        // 3. 执行更新
        scheduleMapper.updateById(schedule);

        // 4. 返回更新后的排班
        return scheduleMapper.selectById(id);
    }

    /**
     * 删除排班
     */
    @Transactional
    public void deleteSchedule(Long id) {
        // 1. 查询排班
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 2. 检查是否有预约（已使用的号源）
        int usedSlots = schedule.getTotalSlots() - schedule.getAvailableSlots();
        if (usedSlots > 0) {
            throw new RuntimeException("该排班已有预约，无法删除");
        }

        // 3. 删除排班
        scheduleMapper.deleteById(id);
    }

    /**
     * 根据ID查询排班
     */
    public Schedule getScheduleById(Long id) {
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }
        return schedule;
    }

    /**
     * 根据ID查询排班详细信息（包含医生、科室等信息）
     */
    public ScheduleWithDetailsDTO getScheduleByIdWithDetails(Long id) {
        ScheduleWithDetailsDTO schedule = scheduleMapper.selectByIdWithDetails(id);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 基于 slotType 和 system_config 计算该排班的挂号费，供前端展示使用
        String normalized = schedule.getSlotType() == null
                ? "NORMAL"
                : schedule.getSlotType().trim().toUpperCase();
        String key;
        switch (normalized) {
            case "EXPERT":
                key = "FEE_EXPERT";
                break;
            case "VIP":
                key = "FEE_VIP";
                break;
            default:
                key = "FEE_NORMAL";
        }

        java.math.BigDecimal fee = systemConfigService.getDecimalOrDefault(key, java.math.BigDecimal.ZERO);
        schedule.setFee(fee);

        return schedule;
    }

    /**
     * 条件查询排班（分页）
     */
    public Map<String, Object> querySchedules(ScheduleQueryRequest request) {
        // 1. 条件查询
        List<ScheduleWithDetailsDTO> schedules = scheduleMapper.selectByConditions(
            request.getDoctorId(),
            request.getDepartmentId(),
            request.getStartDate(),
            request.getEndDate(),
            request.getTimeSlot(),
            request.getSlotType()
        );

        // 2. 统计总数
        int total = scheduleMapper.countByConditions(
            request.getDoctorId(),
            request.getDepartmentId(),
            request.getStartDate(),
            request.getEndDate(),
            request.getTimeSlot(),
            request.getSlotType()
        );

        // 3. 分页处理
        int page = request.getPage();
        int pageSize = request.getPageSize();
        int startIndex = (page - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, schedules.size());

        List<ScheduleWithDetailsDTO> pagedSchedules = schedules.subList(startIndex, endIndex);

        // 4. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", pagedSchedules);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", (total + pageSize - 1) / pageSize);

        return result;
    }

    /**
     * 根据医生ID查询排班列表
     */
    public List<Schedule> getSchedulesByDoctorId(Long doctorId) {
        return scheduleMapper.selectByDoctorId(doctorId);
    }

    /**
     * 根据医生ID查询排班详细信息（包含医生姓名、门诊信息等）
     */
    public List<ScheduleWithDetailsDTO> getSchedulesByDoctorIdWithDetails(Long doctorId) {
        return scheduleMapper.selectByDoctorIdWithDetails(doctorId);
    }

    /**
     * 生成日期列表（批量创建排班时使用）
     */
    private List<Date> generateDateList(Date startDate, Date endDate, Boolean skipWeekends, List<Date> excludeDates) {
        List<Date> dateList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (!calendar.getTime().after(endDate)) {
            Date currentDate = calendar.getTime();

            // 检查是否需要跳过周末
            if (skipWeekends != null && skipWeekends) {
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    continue;
                }
            }

            // 检查是否在排除日期中
            boolean isExcluded = false;
            if (excludeDates != null) {
                for (Date excludeDate : excludeDates) {
                    Calendar excludeCal = Calendar.getInstance();
                    excludeCal.setTime(excludeDate);
                    if (calendar.get(Calendar.YEAR) == excludeCal.get(Calendar.YEAR) &&
                        calendar.get(Calendar.DAY_OF_YEAR) == excludeCal.get(Calendar.DAY_OF_YEAR)) {
                        isExcluded = true;
                        break;
                    }
                }
            }

            if (!isExcluded) {
                dateList.add(currentDate);
            }

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }

    /**
     * 加号（增加号源数量），并自动处理候补队列
     */
    @Transactional
    public Map<String, Object> addSlots(Long id, Integer slotsToAdd) {
        // 1. 查询排班
        Schedule schedule = scheduleMapper.selectById(id);
        if (schedule == null) {
            throw new RuntimeException("排班不存在");
        }

        // 2. 更新号源数量
        schedule.setTotalSlots(schedule.getTotalSlots() + slotsToAdd);
        schedule.setAvailableSlots(schedule.getAvailableSlots() + slotsToAdd);
        scheduleMapper.updateById(schedule);

        // 3. 循环处理候补队列，直到新增的号源被用完或候补队列为空
        int filledCount = 0;
        for (int i = 0; i < slotsToAdd; i++) {
            boolean filled = appointmentService.processNextInWaitlist(id);
            if (filled) {
                filledCount++;
            } else {
                // 候补队列已空，无需继续
                break;
            }
        }

        // 4. 准备返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("schedule", scheduleMapper.selectById(id)); // 返回最新的排班信息
        result.put("filledFromWaitlist", filledCount);
        result.put("message", String.format("成功增加 %d 个号源，并自动为 %d 位候补患者创建了预约。", slotsToAdd, filledCount));

        return result;
    }
}
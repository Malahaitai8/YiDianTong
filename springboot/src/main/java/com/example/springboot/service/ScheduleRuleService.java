package com.example.springboot.service;

import com.example.springboot.dto.*;
import com.example.springboot.entity.*;
import com.example.springboot.mapper.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 排班规则服务
 */
@Service
public class ScheduleRuleService {

    @Resource
    private ScheduleRuleMapper scheduleRuleMapper;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private DepartmentMapper departmentMapper;

    @Resource
    private ClinicMapper clinicMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private AdminMapper adminMapper;

    /**
     * 创建排班规则
     */
    @Transactional
    public ScheduleRuleDetailDTO createRule(CreateScheduleRuleRequest request, String createdBy) {
        // 1. 验证医生是否存在
        if (request.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(request.getDoctorId());
            if (doctor == null) {
                throw new RuntimeException("医生不存在");
            }
        }

        // 2. 验证科室是否存在
        if (request.getDepartmentId() != null) {
            Department department = departmentMapper.selectById(request.getDepartmentId());
            if (department == null) {
                throw new RuntimeException("科室不存在");
            }
        }

        // 3. 验证门诊是否存在
        if (request.getClinicId() != null) {
            Clinic clinic = clinicMapper.selectById(request.getClinicId());
            if (clinic == null) {
                throw new RuntimeException("门诊不存在");
            }
        }

        // 4. 构建实体对象
        ScheduleRule rule = new ScheduleRule();
        rule.setRuleName(request.getRuleName());
        rule.setRuleType(request.getRuleType());
        rule.setDoctorId(request.getDoctorId());
        rule.setDepartmentId(request.getDepartmentId());
        rule.setClinicId(request.getClinicId());
        rule.setWeekDays(convertListToString(request.getWeekDays()));
        rule.setTimeSlots(String.join(",", request.getTimeSlots()));
        rule.setSlotType(request.getSlotType());
        rule.setTotalSlots(request.getTotalSlots());
        rule.setMaxDailySchedules(request.getMaxDailySchedules());
        rule.setMaxContinuousDays(request.getMaxContinuousDays());
        rule.setSkipWeekends(request.getSkipWeekends() != null ? request.getSkipWeekends() : false);
        rule.setSkipHolidays(request.getSkipHolidays() != null ? request.getSkipHolidays() : false);
        rule.setStartDate(request.getStartDate());
        rule.setEndDate(request.getEndDate());
        rule.setStatus("ACTIVE");
        rule.setPriority(request.getPriority() != null ? request.getPriority() : 0);
        rule.setDescription(request.getDescription());
        rule.setCreatedBy(createdBy);

        // 5. 保存到数据库
        scheduleRuleMapper.insert(rule);

        // 6. 返回详情
        return convertToDetailDTO(scheduleRuleMapper.selectById(rule.getId()));
    }

    /**
     * 查询所有排班规则
     */
    public List<ScheduleRuleDetailDTO> getAllRules() {
        List<ScheduleRule> rules = scheduleRuleMapper.selectAll();
        return rules.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据ID查询规则
     */
    public ScheduleRuleDetailDTO getRuleById(Long id) {
        ScheduleRule rule = scheduleRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        return convertToDetailDTO(rule);
    }

    /**
     * 根据医生ID查询规则
     */
    public List<ScheduleRuleDetailDTO> getRulesByDoctorId(Long doctorId) {
        List<ScheduleRule> rules = scheduleRuleMapper.selectByDoctorId(doctorId);
        return rules.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 根据状态查询规则
     */
    public List<ScheduleRuleDetailDTO> getRulesByStatus(String status) {
        List<ScheduleRule> rules = scheduleRuleMapper.selectByStatus(status);
        return rules.stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());
    }

    /**
     * 更新排班规则
     */
    @Transactional
    public ScheduleRuleDetailDTO updateRule(Long id, UpdateScheduleRuleRequest request) {
        // 1. 查询现有规则
        ScheduleRule existingRule = scheduleRuleMapper.selectById(id);
        if (existingRule == null) {
            throw new RuntimeException("规则不存在");
        }

        // 2. 更新字段
        if (request.getRuleName() != null) {
            existingRule.setRuleName(request.getRuleName());
        }
        if (request.getRuleType() != null) {
            existingRule.setRuleType(request.getRuleType());
        }
        if (request.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(request.getDoctorId());
            if (doctor == null) {
                throw new RuntimeException("医生不存在");
            }
            existingRule.setDoctorId(request.getDoctorId());
        }
        if (request.getDepartmentId() != null) {
            existingRule.setDepartmentId(request.getDepartmentId());
        }
        if (request.getClinicId() != null) {
            existingRule.setClinicId(request.getClinicId());
        }
        if (request.getWeekDays() != null) {
            existingRule.setWeekDays(convertListToString(request.getWeekDays()));
        }
        if (request.getTimeSlots() != null) {
            existingRule.setTimeSlots(String.join(",", request.getTimeSlots()));
        }
        if (request.getSlotType() != null) {
            existingRule.setSlotType(request.getSlotType());
        }
        if (request.getTotalSlots() != null) {
            existingRule.setTotalSlots(request.getTotalSlots());
        }
        if (request.getMaxDailySchedules() != null) {
            existingRule.setMaxDailySchedules(request.getMaxDailySchedules());
        }
        if (request.getMaxContinuousDays() != null) {
            existingRule.setMaxContinuousDays(request.getMaxContinuousDays());
        }
        if (request.getSkipWeekends() != null) {
            existingRule.setSkipWeekends(request.getSkipWeekends());
        }
        if (request.getSkipHolidays() != null) {
            existingRule.setSkipHolidays(request.getSkipHolidays());
        }
        if (request.getStartDate() != null) {
            existingRule.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            existingRule.setEndDate(request.getEndDate());
        }
        if (request.getStatus() != null) {
            existingRule.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            existingRule.setPriority(request.getPriority());
        }
        if (request.getDescription() != null) {
            existingRule.setDescription(request.getDescription());
        }

        // 3. 保存到数据库
        scheduleRuleMapper.update(existingRule);

        // 4. 返回详情
        return convertToDetailDTO(scheduleRuleMapper.selectById(id));
    }

    /**
     * 删除排班规则
     */
    @Transactional
    public void deleteRule(Long id) {
        ScheduleRule rule = scheduleRuleMapper.selectById(id);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }
        scheduleRuleMapper.deleteById(id);
    }

    /**
     * 启用规则
     */
    @Transactional
    public void enableRule(Long id) {
        scheduleRuleMapper.updateStatus(id, "ACTIVE");
    }

    /**
     * 禁用规则
     */
    @Transactional
    public void disableRule(Long id) {
        scheduleRuleMapper.updateStatus(id, "INACTIVE");
    }

    /**
     * 应用规则生成排班
     */
    @Transactional
    public Map<String, Object> applyRule(ApplyScheduleRuleRequest request) {
        // 1. 查询规则
        ScheduleRule rule = scheduleRuleMapper.selectById(Long.parseLong(request.getRuleId()));
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }

        // 2. 检查规则状态
        if (!"ACTIVE".equals(rule.getStatus())) {
            throw new RuntimeException("规则未启用，无法应用");
        }

        // 3. 确定应用的日期范围
        Date startDate = request.getApplyStartDate() != null ? request.getApplyStartDate() : rule.getStartDate();
        Date endDate = request.getApplyEndDate() != null ? request.getApplyEndDate() : rule.getEndDate();

        if (endDate == null) {
            // 如果没有结束日期，默认生成未来30天的排班
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.add(Calendar.DAY_OF_MONTH, 30);
            endDate = cal.getTime();
        }

        // 4. 生成日期列表
        List<Date> dateList = generateDateList(startDate, endDate, rule, request.getExcludeDates());

        // 5. 批量创建排班
        int successCount = 0;
        int skipCount = 0;
        List<String> errors = new ArrayList<>();

        String[] timeSlots = rule.getTimeSlots().split(",");
        for (Date date : dateList) {
            for (String timeSlot : timeSlots) {
                try {
                    // 检查是否已存在
                    int exists = scheduleMapper.checkScheduleExists(
                            rule.getDoctorId(),
                            date,
                            timeSlot.trim()
                    );

                    if (exists > 0) {
                        if (request.getOverwriteExisting() != null && request.getOverwriteExisting()) {
                            // 覆盖模式：删除旧排班
                            scheduleMapper.deleteByDoctorDateTimeSlot(rule.getDoctorId(), date, timeSlot.trim());
                        } else {
                            // 跳过模式
                            skipCount++;
                            continue;
                        }
                    }

                    // 创建排班
                    Schedule schedule = new Schedule();
                    schedule.setDoctorId(rule.getDoctorId());
                    schedule.setScheduleDate(date);
                    schedule.setTimeSlot(timeSlot.trim());
                    schedule.setSlotType(rule.getSlotType());
                    schedule.setTotalSlots(rule.getTotalSlots());
                    schedule.setAvailableSlots(rule.getTotalSlots());

                    scheduleMapper.insert(schedule);
                    successCount++;

                } catch (Exception e) {
                    errors.add(String.format("日期 %s 时间段 %s: %s",
                            new SimpleDateFormat("yyyy-MM-dd").format(date),
                            timeSlot,
                            e.getMessage()));
                }
            }
        }

        // 6. 返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);
        result.put("errorCount", errors.size());
        result.put("errors", errors);
        result.put("message", String.format("成功创建 %d 个排班，跳过 %d 个已存在的排班，失败 %d 个",
                successCount, skipCount, errors.size()));

        return result;
    }

    /**
     * 检测规则冲突
     */
    public Map<String, Object> detectConflicts(Long ruleId) {
        ScheduleRule rule = scheduleRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new RuntimeException("规则不存在");
        }

        List<ScheduleRule> allRules = scheduleRuleMapper.selectByStatus("ACTIVE");

        List<Map<String, Object>> conflicts = new ArrayList<>();

        for (ScheduleRule otherRule : allRules) {
            if (otherRule.getId().equals(ruleId)) {
                continue;
            }

            // 检查是否有冲突
            boolean hasConflict = checkRuleConflict(rule, otherRule);
            if (hasConflict) {
                Map<String, Object> conflict = new HashMap<>();
                conflict.put("conflictRuleId", otherRule.getId());
                conflict.put("conflictRuleName", otherRule.getRuleName());
                conflict.put("reason", buildConflictReason(rule, otherRule));
                conflicts.add(conflict);
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("hasConflicts", !conflicts.isEmpty());
        result.put("conflictCount", conflicts.size());
        result.put("conflicts", conflicts);

        return result;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 转换为详情 DTO
     */
    private ScheduleRuleDetailDTO convertToDetailDTO(ScheduleRule rule) {
        ScheduleRuleDetailDTO dto = new ScheduleRuleDetailDTO();
        dto.setId(rule.getId());
        dto.setRuleName(rule.getRuleName());
        dto.setRuleType(rule.getRuleType());
        dto.setRuleTypeName(getRuleTypeName(rule.getRuleType()));
        
        dto.setDoctorId(rule.getDoctorId());
        if (rule.getDoctor() != null) {
            dto.setDoctorName(rule.getDoctor().getName());
            dto.setDoctorTitle(rule.getDoctor().getTitle());
        }
        
        dto.setDepartmentId(rule.getDepartmentId());
        if (rule.getDepartment() != null) {
            dto.setDepartmentName(rule.getDepartment().getName());
        }
        
        dto.setClinicId(rule.getClinicId());
        if (rule.getClinic() != null) {
            dto.setClinicName(rule.getClinic().getName());
        }
        
        dto.setWeekDays(rule.getWeekDays());
        dto.setWeekDaysList(convertStringToList(rule.getWeekDays()));
        dto.setWeekDaysDisplay(getWeekDaysDisplay(rule.getWeekDays()));
        
        dto.setTimeSlots(rule.getTimeSlots());
        dto.setTimeSlotsList(Arrays.asList(rule.getTimeSlots().split(",")));
        dto.setTimeSlotsDisplay(getTimeSlotsDisplay(rule.getTimeSlots()));
        
        dto.setStartDate(rule.getStartDate());
        dto.setEndDate(rule.getEndDate());
        dto.setSlotType(rule.getSlotType());
        dto.setSlotTypeName(getSlotTypeName(rule.getSlotType()));
        dto.setTotalSlots(rule.getTotalSlots());
        dto.setMaxDailySchedules(rule.getMaxDailySchedules());
        dto.setMaxContinuousDays(rule.getMaxContinuousDays());
        dto.setSkipWeekends(rule.getSkipWeekends());
        dto.setSkipHolidays(rule.getSkipHolidays());
        dto.setStatus(rule.getStatus());
        dto.setStatusName(getStatusName(rule.getStatus()));
        dto.setPriority(rule.getPriority());
        dto.setDescription(rule.getDescription());
        dto.setCreatedBy(rule.getCreatedBy());
        
        // 查询创建人姓名：先通过username查User表获取userId，再查Admin表获取name
        if (rule.getCreatedBy() != null) {
            User creator = userMapper.selectByUsername(rule.getCreatedBy());
            if (creator != null) {
                // 通过userId查询Admin表获取管理员姓名
                Admin admin = adminMapper.selectByUserId(creator.getId());
                if (admin != null && admin.getName() != null) {
                    dto.setCreatedByName(admin.getName());
                } else {
                    // 如果没有找到管理员信息，使用username
                    dto.setCreatedByName(creator.getUsername());
                }
            }
        }
        
        dto.setCreatedAt(rule.getCreatedAt());
        dto.setUpdatedAt(rule.getUpdatedAt());
        
        return dto;
    }

    /**
     * 生成日期列表
     */
    private List<Date> generateDateList(Date startDate, Date endDate, ScheduleRule rule, List<Date> excludeDates) {
        List<Date> dateList = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);

        Set<String> excludeDateStrings = new HashSet<>();
        if (excludeDates != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Date date : excludeDates) {
                excludeDateStrings.add(sdf.format(date));
            }
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Integer> weekDaysList = convertStringToList(rule.getWeekDays());

        while (!cal.getTime().after(endDate)) {
            Date currentDate = cal.getTime();
            String dateString = sdf.format(currentDate);

            // 检查是否在排除列表中
            if (excludeDateStrings.contains(dateString)) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                continue;
            }

            // 检查是否跳过周末
            if (rule.getSkipWeekends() != null && rule.getSkipWeekends()) {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    continue;
                }
            }

            // 检查是否符合规则的星期要求
            if (weekDaysList != null && !weekDaysList.isEmpty()) {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                // Calendar.DAY_OF_WEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
                // 转换为我们的格式: 1=Monday, 2=Tuesday, ..., 7=Sunday
                int ourDayOfWeek = dayOfWeek == Calendar.SUNDAY ? 7 : dayOfWeek - 1;
                
                if (!weekDaysList.contains(ourDayOfWeek)) {
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    continue;
                }
            }

            dateList.add(currentDate);
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        return dateList;
    }

    /**
     * 检查两个规则是否冲突
     */
    private boolean checkRuleConflict(ScheduleRule rule1, ScheduleRule rule2) {
        // 1. 必须是同一个医生
        if (!Objects.equals(rule1.getDoctorId(), rule2.getDoctorId())) {
            return false;
        }

        // 2. 日期范围必须有重叠
        if (rule1.getEndDate() != null && rule2.getStartDate() != null &&
                rule1.getEndDate().before(rule2.getStartDate())) {
            return false;
        }
        if (rule2.getEndDate() != null && rule1.getStartDate() != null &&
                rule2.getEndDate().before(rule1.getStartDate())) {
            return false;
        }

        return true;
    }

    /**
     * 构建冲突原因描述
     */
    private String buildConflictReason(ScheduleRule rule1, ScheduleRule rule2) {
        return String.format("规则在医生ID=%d、日期范围和时间段上存在重叠", rule1.getDoctorId());
    }

    /**
     * 转换列表为字符串
     */
    private String convertListToString(List<Integer> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    /**
     * 转换字符串为列表
     */
    private List<Integer> convertStringToList(String str) {
        if (str == null || str.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * 获取规则类型名称
     */
    private String getRuleTypeName(String ruleType) {
        switch (ruleType) {
            case "weekly": return "每周固定";
            case "custom": return "自定义";
            case "template": return "模板";
            default: return ruleType;
        }
    }

    /**
     * 获取号别名称
     */
    private String getSlotTypeName(String slotType) {
        switch (slotType) {
            case "normal": return "普通号";
            case "expert": return "专家号";
            case "vip": return "特需号";
            default: return slotType;
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(String status) {
        switch (status) {
            case "ACTIVE": return "启用";
            case "INACTIVE": return "禁用";
            case "EXPIRED": return "已过期";
            default: return status;
        }
    }

    /**
     * 获取星期显示
     */
    private String getWeekDaysDisplay(String weekDays) {
        if (weekDays == null || weekDays.isEmpty()) {
            return "";
        }
        String[] days = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        return Arrays.stream(weekDays.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .map(i -> days[i - 1])
                .collect(Collectors.joining("、"));
    }

    /**
     * 获取时段显示
     */
    private String getTimeSlotsDisplay(String timeSlots) {
        if (timeSlots == null || timeSlots.isEmpty()) {
            return "";
        }
        return Arrays.stream(timeSlots.split(","))
                .map(String::trim)
                .map(slot -> {
                    switch (slot) {
                        case "morning": return "上午";
                        case "afternoon": return "下午";
                        case "evening": return "晚上";
                        default: return slot;
                    }
                })
                .collect(Collectors.joining("、"));
    }
}


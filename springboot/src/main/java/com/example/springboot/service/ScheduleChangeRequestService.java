package com.example.springboot.service;

import com.example.springboot.dto.ApproveScheduleChangeRequest;
import com.example.springboot.dto.ScheduleChangeRequestDTO;
import com.example.springboot.dto.ScheduleChangeRequestItem;
import com.example.springboot.entity.Doctor;
import com.example.springboot.entity.Schedule;
import com.example.springboot.entity.SystemConfig;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 调班申请服务
 * 使用system_config表的JSON字段存储调班申请，不修改数据库结构
 */
@Service
public class ScheduleChangeRequestService {

    private static final String CONFIG_KEY = "schedule_change_requests";

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private ScheduleMapper scheduleMapper;

    @Resource
    private DoctorMapper doctorMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 加载配置（从system_config表）
     */
    public Map<String, Object> loadConfig() {
        SystemConfig cfg = systemConfigService.selectByKey(CONFIG_KEY);
        if (cfg == null) {
            Map<String, Object> init = new HashMap<>();
            init.put("version", 1);
            init.put("items", new ArrayList<>());
            writeConfig(init);
            return init;
        }
        String value = cfg.getValue();
        if (!StringUtils.hasText(value)) {
            Map<String, Object> init = new HashMap<>();
            init.put("version", 1);
            init.put("items", new ArrayList<>());
            writeConfig(init);
            return init;
        }
        try {
            return objectMapper.readValue(value, new TypeReference<Map<String, Object>>(){});
        } catch (Exception e) {
            throw new RuntimeException("Invalid schedule_change_requests JSON", e);
        }
    }

    /**
     * 写入配置（到system_config表）
     */
    private void writeConfig(Map<String, Object> content) {
        try {
            String json = objectMapper.writeValueAsString(content);
            SystemConfig existing = systemConfigService.selectByKey(CONFIG_KEY);
            if (existing == null) {
                SystemConfig sc = new SystemConfig();
                sc.setKey(CONFIG_KEY);
                sc.setValue(json);
                sc.setDescription("Schedule change requests queue");
                systemConfigService.insert(sc);
            } else {
                systemConfigService.updateValueByKey(CONFIG_KEY, json);
            }
        } catch (Exception e) {
            throw new RuntimeException("Persist schedule_change_requests failed", e);
        }
    }

    /**
     * 获取items列表
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getItems(Map<String, Object> cfg) {
        Object items = cfg.get("items");
        if (items == null) {
            List<Map<String, Object>> list = new ArrayList<>();
            cfg.put("items", list);
            return list;
        }
        return (List<Map<String, Object>>) items;
    }

    /**
     * 医生提交调班申请
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleChangeRequestItem submit(ScheduleChangeRequestDTO dto) {
        // 1. 基本验证
        if (dto.getScheduleId() == null) {
            throw new IllegalArgumentException("scheduleId is required");
        }
        if (dto.getRequestedByUserId() == null) {
            throw new IllegalArgumentException("requestedByUserId is required");
        }

        // 2. 查询排班信息
        Schedule schedule = scheduleMapper.selectById(dto.getScheduleId());
        if (schedule == null) {
            throw new IllegalArgumentException("Schedule not found");
        }

        // 3. 推导医生ID
        if (dto.getDoctorId() == null) {
            Doctor currentDoctor = doctorMapper.selectByUserId(dto.getRequestedByUserId());
            if (currentDoctor == null) {
                throw new IllegalArgumentException("Doctor not found for current user");
            }
            dto.setDoctorId(currentDoctor.getId());
        }

        // 4. 验证医生是否拥有该排班
        if (!Objects.equals(schedule.getDoctorId(), dto.getDoctorId())) {
            throw new IllegalArgumentException("This schedule does not belong to current doctor");
        }

        // 5. 至少有一个字段要修改
        if (dto.getNewScheduleDate() == null && dto.getNewTimeSlot() == null 
            && dto.getNewSlotType() == null && dto.getNewTotalSlots() == null) {
            throw new IllegalArgumentException("At least one field must be changed");
        }

        // 6. 加载配置
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);

        // 7. 创建申请记录
        String id = UUID.randomUUID().toString();
        long now = System.currentTimeMillis();
        Map<String, Object> entry = new HashMap<>();
        entry.put("id", id);
        entry.put("scheduleId", dto.getScheduleId());
        entry.put("requestedByUserId", dto.getRequestedByUserId());
        entry.put("doctorId", dto.getDoctorId());

        // 原始排班信息
        entry.put("originalScheduleDate", dateFormat.format(schedule.getScheduleDate()));
        entry.put("originalTimeSlot", schedule.getTimeSlot());
        entry.put("originalSlotType", schedule.getSlotType());
        entry.put("originalTotalSlots", schedule.getTotalSlots());

        // 新的排班信息
        entry.put("newScheduleDate", dto.getNewScheduleDate());
        entry.put("newTimeSlot", dto.getNewTimeSlot());
        entry.put("newSlotType", dto.getNewSlotType());
        entry.put("newTotalSlots", dto.getNewTotalSlots());

        entry.put("reason", dto.getReason());
        entry.put("status", "PENDING");
        entry.put("createdAt", now);
        entry.put("updatedAt", now);

        items.add(entry);

        // 8. 更新版本并持久化
        int version = ((Number) cfg.getOrDefault("version", 1)).intValue();
        cfg.put("version", version + 1);
        writeConfig(cfg);

        return toItem(entry);
    }

    /**
     * 查询所有调班申请（管理员）
     */
    public List<ScheduleChangeRequestItem> listAll(Optional<String> statusOpt) {
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);
        List<ScheduleChangeRequestItem> result = new ArrayList<>();
        
        for (Map<String, Object> it : items) {
            ScheduleChangeRequestItem item = toItem(it);
            if (statusOpt.isPresent()) {
                if (statusOpt.get().equalsIgnoreCase(item.getStatus())) {
                    result.add(item);
                }
            } else {
                result.add(item);
            }
        }
        
        return result;
    }

    /**
     * 按医生ID查询调班申请
     */
    public List<ScheduleChangeRequestItem> listByDoctor(Long doctorId) {
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);
        List<ScheduleChangeRequestItem> result = new ArrayList<>();
        
        for (Map<String, Object> it : items) {
            if (Objects.equals(asLong(it.get("doctorId")), doctorId)) {
                result.add(toItem(it));
            }
        }
        
        return result;
    }

    /**
     * 当前医生查询自己的调班申请
     */
    public List<ScheduleChangeRequestItem> listByCurrentDoctor(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        Doctor doctor = doctorMapper.selectByUserId(userId);
        if (doctor == null || doctor.getId() == null) {
            return Collections.emptyList();
        }
        return listByDoctor(doctor.getId());
    }

    /**
     * 管理员审核调班申请
     */
    @Transactional(rollbackFor = Exception.class)
    public ScheduleChangeRequestItem review(ApproveScheduleChangeRequest req, String adminUsername) {
        Map<String, Object> cfg = loadConfig();
        List<Map<String, Object>> items = getItems(cfg);
        
        // 1. 查找申请
        Map<String, Object> target = null;
        for (Map<String, Object> it : items) {
            if (Objects.equals(String.valueOf(it.get("id")), req.getId())) {
                target = it;
                break;
            }
        }
        if (target == null) {
            throw new IllegalArgumentException("Request not found");
        }

        // 2. 验证审核动作
        String action = req.getAction();
        if (!"APPROVE".equalsIgnoreCase(action) && !"REJECT".equalsIgnoreCase(action)) {
            throw new IllegalArgumentException("Action must be APPROVE or REJECT");
        }

        // 3. 处理审核
        if ("APPROVE".equalsIgnoreCase(action)) {
            // 通过：修改排班表
            Long scheduleId = asLong(target.get("scheduleId"));
            Schedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule == null) {
                throw new IllegalStateException("Schedule not found");
            }

            // 应用修改
            if (target.get("newScheduleDate") != null) {
                try {
                    Date newDate = dateFormat.parse((String) target.get("newScheduleDate"));
                    schedule.setScheduleDate(newDate);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Invalid newScheduleDate format");
                }
            }
            if (target.get("newTimeSlot") != null) {
                schedule.setTimeSlot((String) target.get("newTimeSlot"));
            }
            if (target.get("newSlotType") != null) {
                schedule.setSlotType((String) target.get("newSlotType"));
            }
            if (target.get("newTotalSlots") != null) {
                Integer newTotal = asInteger(target.get("newTotalSlots"));
                Integer oldTotal = schedule.getTotalSlots();
                Integer oldAvailable = schedule.getAvailableSlots();
                
                // 调整可用号源：保持已预约数量不变
                Integer booked = oldTotal - oldAvailable;
                Integer newAvailable = newTotal - booked;
                if (newAvailable < 0) {
                    throw new IllegalArgumentException("New total slots is less than booked slots");
                }
                
                schedule.setTotalSlots(newTotal);
                schedule.setAvailableSlots(newAvailable);
            }

            // 更新数据库
            scheduleMapper.updateById(schedule);

            target.put("status", "APPROVED");
            target.put("approvedBy", adminUsername);
            target.put("approvedAt", System.currentTimeMillis());
            target.put("rejectionReason", null);
        } else {
            // 拒绝
            target.put("status", "REJECTED");
            target.put("approvedBy", adminUsername);
            target.put("approvedAt", System.currentTimeMillis());
            target.put("rejectionReason", req.getReason());
        }
        
        target.put("updatedAt", System.currentTimeMillis());

        // 4. 更新版本并持久化
        int version = ((Number) cfg.getOrDefault("version", 1)).intValue();
        cfg.put("version", version + 1);
        writeConfig(cfg);
        
        return toItem(target);
    }

    /**
     * 将Map转换为ScheduleChangeRequestItem
     */
    private ScheduleChangeRequestItem toItem(Map<String, Object> it) {
        ScheduleChangeRequestItem item = new ScheduleChangeRequestItem();
        item.setId(String.valueOf(it.get("id")));
        item.setScheduleId(asLong(it.get("scheduleId")));
        item.setRequestedByUserId(asLong(it.get("requestedByUserId")));
        item.setDoctorId(asLong(it.get("doctorId")));
        
        // 获取医生姓名
        Long doctorId = asLong(it.get("doctorId"));
        if (doctorId != null) {
            Doctor doctor = doctorMapper.selectById(doctorId);
            if (doctor != null) {
                item.setDoctorName(doctor.getName());
            }
        }
        
        // 原始排班信息
        item.setOriginalScheduleDate((String) it.get("originalScheduleDate"));
        item.setOriginalTimeSlot((String) it.get("originalTimeSlot"));
        item.setOriginalSlotType((String) it.get("originalSlotType"));
        item.setOriginalTotalSlots(asInteger(it.get("originalTotalSlots")));
        
        // 新的排班信息
        item.setNewScheduleDate((String) it.get("newScheduleDate"));
        item.setNewTimeSlot((String) it.get("newTimeSlot"));
        item.setNewSlotType((String) it.get("newSlotType"));
        item.setNewTotalSlots(asInteger(it.get("newTotalSlots")));
        
        item.setReason((String) it.get("reason"));
        item.setStatus((String) it.get("status"));
        item.setApprovedBy((String) it.get("approvedBy"));
        item.setApprovedAt(asLong(it.get("approvedAt")));
        item.setRejectionReason((String) it.get("rejectionReason"));
        item.setCreatedAt(asLong(it.get("createdAt")));
        item.setUpdatedAt(asLong(it.get("updatedAt")));
        
        return item;
    }

    /**
     * 辅助方法：将Object转换为Long
     */
    private Long asLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 辅助方法：将Object转换为Integer
     */
    private Integer asInteger(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return null;
        }
    }
}


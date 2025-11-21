// 文件路径: src/main/java/com/example/springboot/service/WaitlistService.java
package com.example.springboot.service;

import com.example.springboot.dto.WaitlistInfoDTO; // <-- [新增] 导入
import com.example.springboot.entity.Schedule;
import com.example.springboot.entity.Doctor;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.DoctorMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate; // <-- [新增] 导入
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WaitlistService {

    @Resource
    private ScheduleMapper scheduleMapper; // <-- [保留] 仍然需要

    @Resource
    private DoctorMapper doctorMapper;

    // [修改] 注入 RedisTemplate (使用 Object, 配合 RedisConfig)
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    // [修改] 移除 WaitlistMapper

    // 定义 Redis Key 的前缀
    // 候补队列 (ZSET): "waitlist:schedule:{scheduleId}" -> ZSET [ patientId, joinTime ]
    private static final String WAITLIST_KEY_PREFIX = "waitlist:schedule:";
    // 患者的候补列表 (SET): "waitlist:patient:{patientId}" -> SET [ "scheduleId1", "scheduleId2" ]
    private static final String PATIENT_KEY_PREFIX = "waitlist:patient:";


    /** [重构] 加入候补队列 (使用 Redis ZSET) */
    public void addToQueue(Long patientId, Long scheduleId) {
        Schedule schedule = scheduleMapper.selectById(scheduleId);
        if (schedule == null) {
            throw new CustomerException("排班不存在");
        }
        if (schedule.getAvailableSlots() != null && schedule.getAvailableSlots() > 0) {
            throw new CustomerException("当前仍有号源，可直接预约");
        }

        String key = WAITLIST_KEY_PREFIX + scheduleId;

        // 检查是否已在队列中 (score不为null说明已存在)
        Double score = redisTemplate.opsForZSet().score(key, patientId);
        if (score != null) {
            throw new CustomerException("已在候补队列中，请勿重复提交");
        }

        // 1. 加入 ZSET，分数为当前时间戳
        long joinTime = new Date().getTime();
        redisTemplate.opsForZSet().add(key, patientId, (double) joinTime);

        // 2. 额外维护一个反向索引 (用于 "我的候补" 查询)
        String patientKey = PATIENT_KEY_PREFIX + patientId;
        redisTemplate.opsForSet().add(patientKey, String.valueOf(scheduleId));
    }

    /** [重构] 弹出队首 (从 ZSET 中弹出分数最低的) */
    public Long popNext(Long scheduleId) {
        String key = WAITLIST_KEY_PREFIX + scheduleId;

        // 弹出分数最低（即最早加入）的 1 个元素
        // 注意: popMin 返回一个包含 TypedTuple 的 Set，我们需要从中提取值
        Set<Object> result = redisTemplate.opsForZSet().popMin(key, 1)
                .stream()
                .map(tuple -> ((org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>) tuple).getValue())
                .collect(Collectors.toSet());

        if (result.isEmpty()) {
            return null; // 队列为空
        }

        // (注意: 序列化配置会导致这里可能是Integer或String)
        // 确保使用 Long 类型
        Long patientId = ((Number) result.iterator().next()).longValue();

        // 3. (清理) 从该患者的反向索引中移除这个排班
        String patientKey = PATIENT_KEY_PREFIX + patientId;
        redisTemplate.opsForSet().remove(patientKey, String.valueOf(scheduleId));

        return patientId;
    }

    /** [重构] 根据患者ID查询候补列表 */
    public List<WaitlistInfoDTO> listByPatient(Long patientId) {
        String patientKey = PATIENT_KEY_PREFIX + patientId;

        // 1. 获取该患者候补的所有 scheduleId
        Set<Object> scheduleIdObjects = redisTemplate.opsForSet().members(patientKey);
        if (scheduleIdObjects == null || scheduleIdObjects.isEmpty()) {
            return List.of(); // 返回空列表
        }

        // 2. 遍历，查询每个队列中该 patientId 的排名，并补充展示所需信息
        return scheduleIdObjects.stream()
                .map(obj -> {
                    if (!(obj instanceof Number) && !(obj instanceof String)) {
                        return null;
                    }
                    try {
                        Long scheduleId = Long.valueOf(String.valueOf(obj));
                        String queueKey = WAITLIST_KEY_PREFIX + scheduleId;

                        Long rank = redisTemplate.opsForZSet().rank(queueKey, patientId);
                        Long queueSize = redisTemplate.opsForZSet().size(queueKey);
                        if (rank == null) {
                            return null;
                        }

                        WaitlistInfoDTO dto = new WaitlistInfoDTO(scheduleId, rank, queueSize);

                        // 额外补充：医生与排班信息
                        Schedule s = scheduleMapper.selectById(scheduleId);
                        if (s != null) {
                            dto.setScheduleDate(s.getScheduleDate());
                            dto.setTimeSlot(s.getTimeSlot());
                            dto.setTimeSlotName(timeSlotToCn(s.getTimeSlot()));
                            dto.setDoctorId(s.getDoctorId());
                            try {
                                Doctor d = doctorMapper.selectById(s.getDoctorId());
                                if (d != null) dto.setDoctorName(d.getName());
                            } catch (Exception ignored) {}
                        }
                        return dto;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid scheduleId format in Redis set: " + obj);
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** [新增] 从候补队列中移除 */
    public void removeFromQueue(Long patientId, Long scheduleId) {
        String queueKey = WAITLIST_KEY_PREFIX + scheduleId;
        String patientKey = PATIENT_KEY_PREFIX + patientId;
        // 1. 从 ZSET 中移除患者
        redisTemplate.opsForZSet().remove(queueKey, patientId);
        // 2. 从患者的反向索引 SET 中移除 scheduleId
        redisTemplate.opsForSet().remove(patientKey, String.valueOf(scheduleId));
    }

    private String timeSlotToCn(String timeSlot) {
        if (timeSlot == null) return null;
        switch (timeSlot.toLowerCase()) {
            case "morning": return "上午";
            case "afternoon": return "下午";
            case "evening": return "晚上";
            default: return timeSlot;
        }
    }

}
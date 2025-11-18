// 文件路径: src/main/java/com/example/springboot/service/WaitlistService.java
package com.example.springboot.service;

import com.example.springboot.dto.WaitlistInfoDTO; // <-- [新增] 导入
import com.example.springboot.entity.Schedule;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.ScheduleMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate; // <-- [新增] 导入
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WaitlistService {

    @Resource
    private ScheduleMapper scheduleMapper; // <-- [保留] 仍然需要

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

        // 2. 遍历，查询每个队列中该 patientId 的排名
        return scheduleIdObjects.stream().map(obj -> {
                    Long scheduleId = Long.parseLong(String.valueOf(obj));
                    String queueKey = WAITLIST_KEY_PREFIX + scheduleId;

                    // 获取我的排名 (rank 是从 0 开始的)
                    Long rank = redisTemplate.opsForZSet().rank(queueKey, patientId);

                    // 获取总排队人数
                    Long queueSize = redisTemplate.opsForZSet().size(queueKey);

                    // 如果排名为null (可能刚被弹出，但反向索引还未清除)，则跳过
                    if (rank == null) {
                        return null;
                    }

                    return new WaitlistInfoDTO(scheduleId, rank, queueSize);
                }).filter(dto -> dto != null) // 过滤掉已处理的
                .collect(Collectors.toList());
    }

}
// 文件路径: src/main/java/com/example/springboot/service/WaitlistService.java
package com.example.springboot.service;

import com.example.springboot.dto.WaitlistInfoDTO;
import com.example.springboot.entity.Doctor;
import com.example.springboot.entity.Schedule;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.DoctorMapper;
import com.example.springboot.mapper.ScheduleMapper;
import com.example.springboot.mapper.WaitlistMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WaitlistService {

    private static final Logger logger = LoggerFactory.getLogger(WaitlistService.class);

    @Resource
    private ScheduleMapper scheduleMapper; // <-- [保留] 仍然需要

    @Resource
    private DoctorMapper doctorMapper;

    @Resource
    private WaitlistMapper waitlistMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    private static final String WAITLIST_KEY_PREFIX = "waitlist:schedule:";
    private static final String PATIENT_KEY_PREFIX = "waitlist:patient:";
    private static final String STATS_CACHE_KEY_PREFIX = "waitlist:stats:";
    private static final long STATS_CACHE_EXPIRE_SECONDS = 1800; // 缓存30分钟

    public void addToQueue(Long patientId, Long scheduleId) {
        Waitlist waitlist = waitlistMapper.selectByScheduleAndPatient(scheduleId, patientId);
        if (waitlist == null) {
            throw new CustomerException("候补记录不存在");
        }
        addToQueue(waitlist);
    }

    public void addToQueue(Waitlist waitlist) {
        addInternal(waitlist, false);
    }

    public void requeue(Waitlist waitlist) {
        addInternal(waitlist, true);
    }

    private void addInternal(Waitlist waitlist, boolean skipAvailabilityCheck) {
        // 仅在必要时打开 DEBUG 观察候补入队流程，正常运行不刷屏
        logger.debug("addInternal start waitlistId={} scheduleId={} patientId={}",
            waitlist != null ? waitlist.getId() : null,
            waitlist != null ? waitlist.getScheduleId() : null,
            waitlist != null ? waitlist.getPatientId() : null);
            
        if (waitlist == null || waitlist.getId() == null) {
            logger.error("addInternal failed: waitlist is null or id is null");
            throw new CustomerException("候补记录不存在");
        }

        Schedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
        if (schedule == null) {
            logger.error("addInternal failed: schedule not found scheduleId={}", waitlist.getScheduleId());
            throw new CustomerException("排班不存在");
        }
        if (!skipAvailabilityCheck
                && schedule.getAvailableSlots() != null
                && schedule.getAvailableSlots() > 0) {
            logger.warn("addInternal failed: slots available scheduleId={} availableSlots={}", 
                waitlist.getScheduleId(), schedule.getAvailableSlots());
            throw new CustomerException("当前仍有号源，可直接预约");
        }

        String queueKey = WAITLIST_KEY_PREFIX + waitlist.getScheduleId();
        String member = String.valueOf(waitlist.getId());
        logger.debug("addInternal checking Redis queueKey={} member={}", queueKey, member);
        
        Double score = stringRedisTemplate.opsForZSet().score(queueKey, member);
        if (score != null) {
            logger.warn("addInternal failed: already in queue queueKey={} member={} score={}", queueKey, member, score);
            throw new CustomerException("已在候补队列中，请勿重复提交");
        }

        long joinTime = waitlist.getJoinTime() != null ? waitlist.getJoinTime().getTime() : System.currentTimeMillis();
        logger.debug("addInternal writing to Redis queueKey={} member={} score={}", queueKey, member, joinTime);
        
        Boolean addResult = stringRedisTemplate.opsForZSet().add(queueKey, member, (double) joinTime);
        logger.debug("addInternal Redis ZADD result={} queueKey={} member={}", addResult, queueKey, member);

        String patientKey = PATIENT_KEY_PREFIX + waitlist.getPatientId();
        Long saddResult = stringRedisTemplate.opsForSet().add(patientKey, member);
        logger.debug("addInternal Redis SADD result={} patientKey={} member={}", saddResult, patientKey, member);
        
        // 清理遗留的 scheduleId 索引
        stringRedisTemplate.opsForSet().remove(patientKey, String.valueOf(waitlist.getScheduleId()));
        
        logger.debug("addInternal completed successfully waitlistId={} scheduleId={}", waitlist.getId(), waitlist.getScheduleId());
    }

    public Waitlist popNext(Long scheduleId) {
        String key = WAITLIST_KEY_PREFIX + scheduleId;
        
        // [修复] 循环查找有效的候补记录（跳过已处理的记录）
        int maxAttempts = 10; // 最多尝试10次，避免无限循环
        int attempts = 0;
        
        while (attempts < maxAttempts) {
            Set<ZSetOperations.TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet().popMin(key, 1);
            if (tuples == null || tuples.isEmpty()) {
                // Redis 队列为空时，回退到数据库按 join_time 查询
                Waitlist fallback = waitlistMapper.selectNextWaiting(scheduleId);
                if (fallback != null) {
                    cleanupPatientIndex(fallback.getPatientId(), fallback.getId(), scheduleId);
                }
                return fallback;
            }
            
            Object rawValue = tuples.iterator().next().getValue();
            Waitlist waitlist = findFromQueueValue(scheduleId, rawValue);
            
            if (waitlist == null) {
                attempts++;
                continue; // 继续查找下一个
            }
            
            // [修复] 检查候补记录状态，只处理WAITING状态的记录
            if (!"WAITING".equalsIgnoreCase(waitlist.getStatus()) && 
                !"NOTIFIED".equalsIgnoreCase(waitlist.getStatus())) {
                // 如果状态不是WAITING或NOTIFIED（比如已经是GRANTED），跳过这条记录
                logger.warn("跳过已处理的候补记录 waitlistId={}, status={}", waitlist.getId(), waitlist.getStatus());
                attempts++;
                continue; // 继续查找下一个
            }
            
            cleanupPatientIndex(waitlist.getPatientId(), waitlist.getId(), scheduleId);
            return waitlist;
        }
        
        // 如果尝试多次后仍未找到有效记录，返回null
        logger.warn("候补队列中未找到有效的候补记录 scheduleId={}, attempts={}", scheduleId, attempts);
        return null;
    }

    public List<WaitlistInfoDTO> listByPatient(Long patientId) {
        String patientKey = PATIENT_KEY_PREFIX + patientId;
        Set<String> rawValues = stringRedisTemplate.opsForSet().members(patientKey);
        if (rawValues == null || rawValues.isEmpty()) {
            // Redis 无数据时回退数据库
            return waitlistMapper.selectByPatientId(patientId).stream()
                    .filter(Objects::nonNull)
                    .filter(waitlist -> {
                        String status = waitlist.getStatus();
                        return "WAITING".equalsIgnoreCase(status) || "NOTIFIED".equalsIgnoreCase(status);
                    })
                    .map(this::buildInfoDTO)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }

        return rawValues.stream()
                .map(value -> findFromPatientIndex(patientId, value))
                .filter(Objects::nonNull)
                // [修复] 过滤掉已处理的候补记录（GRANTED状态），避免显示"既挂号了也候补"
                .filter(waitlist -> {
                    String status = waitlist.getStatus();
                    // 只返回WAITING或NOTIFIED状态的候补记录
                    return "WAITING".equalsIgnoreCase(status) || "NOTIFIED".equalsIgnoreCase(status);
                })
                .map(this::buildInfoDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void removeFromQueue(Long patientId, Long scheduleId) {
        Waitlist waitlist = waitlistMapper.selectByScheduleAndPatient(scheduleId, patientId);
        if (waitlist != null) {
            removeFromQueue(waitlist);
        }
    }

    public void removeFromQueue(Waitlist waitlist) {
        if (waitlist == null) {
            return;
        }
        String queueKey = WAITLIST_KEY_PREFIX + waitlist.getScheduleId();
        stringRedisTemplate.opsForZSet().remove(queueKey, String.valueOf(waitlist.getId()));
        // 清理遗留的 patientId 记录（兼容老数据）
        stringRedisTemplate.opsForZSet().remove(queueKey, String.valueOf(waitlist.getPatientId()));
        cleanupPatientIndex(waitlist.getPatientId(), waitlist.getId(), waitlist.getScheduleId());
        
        // 清除相关统计数据缓存，确保数据实时性
        invalidateStatsCache(waitlist.getScheduleId());
    }
    
    /**
     * 清除候补统计数据缓存
     * 当候补状态发生变化时（成功、失败、退出等），清除相关缓存以确保数据实时性
     */
    private void invalidateStatsCache(Long scheduleId) {
        try {
            Schedule schedule = scheduleMapper.selectById(scheduleId);
            if (schedule != null) {
                String statsKey = STATS_CACHE_KEY_PREFIX + schedule.getDoctorId() + ":" + schedule.getTimeSlot();
                redisTemplate.delete(statsKey);
            }
        } catch (Exception e) {
            // 清除缓存失败不影响主流程
        }
    }
    
    /**
     * 清除候补统计数据缓存（公开方法，供外部调用）
     * 当候补成功转为预约时，应调用此方法清除缓存
     */
    public void invalidateStatsCacheBySchedule(Long scheduleId) {
        invalidateStatsCache(scheduleId);
    }

    private Waitlist findFromQueueValue(Long scheduleId, Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            Long candidate = Long.valueOf(String.valueOf(rawValue));
            Waitlist waitlist = waitlistMapper.selectById(candidate);
            if (waitlist != null) {
                return waitlist;
            }
            // 兼容旧版：value 为 patientId
            if (scheduleId != null) {
                return waitlistMapper.selectByScheduleAndPatient(scheduleId, candidate);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private Waitlist findFromPatientIndex(Long patientId, Object rawValue) {
        if (rawValue == null) {
            return null;
        }
        try {
            Long candidate = Long.valueOf(String.valueOf(rawValue));
            Waitlist waitlist = waitlistMapper.selectById(candidate);
            if (waitlist != null) {
                return waitlist;
            }
            // 兼容旧版：value 为 scheduleId
            Waitlist legacy = waitlistMapper.selectByScheduleAndPatient(candidate, patientId);
            if (legacy != null) {
                // migrate legacy entry
                String patientKey = PATIENT_KEY_PREFIX + patientId;
                redisTemplate.opsForSet().add(patientKey, String.valueOf(legacy.getId()));
                redisTemplate.opsForSet().remove(patientKey, String.valueOf(candidate));
            }
            return legacy;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private void cleanupPatientIndex(Long patientId, Long waitlistId, Long scheduleId) {
        String patientKey = PATIENT_KEY_PREFIX + patientId;
        stringRedisTemplate.opsForSet().remove(patientKey, String.valueOf(waitlistId));
        if (scheduleId != null) {
            stringRedisTemplate.opsForSet().remove(patientKey, String.valueOf(scheduleId));
        }
    }

    private WaitlistInfoDTO buildInfoDTO(Waitlist waitlist) {
        String queueKey = WAITLIST_KEY_PREFIX + waitlist.getScheduleId();
        Long rank = stringRedisTemplate.opsForZSet().rank(queueKey, String.valueOf(waitlist.getId()));
        if (rank == null) {
            // 兼容旧版：排序依据 patientId
            rank = stringRedisTemplate.opsForZSet().rank(queueKey, String.valueOf(waitlist.getPatientId()));
            if (rank == null) {
                return null;
            }
        }
        Long queueSize = stringRedisTemplate.opsForZSet().size(queueKey);
        WaitlistInfoDTO dto = new WaitlistInfoDTO(waitlist.getId(), waitlist.getScheduleId(), rank, queueSize);

        Schedule schedule = scheduleMapper.selectById(waitlist.getScheduleId());
        if (schedule != null) {
            dto.setScheduleDate(schedule.getScheduleDate());
            dto.setTimeSlot(schedule.getTimeSlot());
            dto.setTimeSlotName(timeSlotToCn(schedule.getTimeSlot()));
            dto.setDoctorId(schedule.getDoctorId());
            try {
                Doctor doctor = doctorMapper.selectById(schedule.getDoctorId());
                if (doctor != null) {
                    dto.setDoctorName(doctor.getName());
                }
            } catch (Exception ignored) {
            }

            // 计算候补可视化统计数据（使用Redis缓存）
            try {
                String statsKey = STATS_CACHE_KEY_PREFIX + schedule.getDoctorId() + ":" + schedule.getTimeSlot();
                
                // 尝试从Redis缓存获取统计数据
                Object cachedStats = redisTemplate.opsForValue().get(statsKey);
                if (cachedStats instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> stats = (java.util.Map<String, Object>) cachedStats;
                    dto.setSuccessRate(stats.get("successRate") != null ? 
                        ((Number) stats.get("successRate")).doubleValue() : 0.0);
                    dto.setAvgWaitTime(stats.get("avgWaitTime") != null ? 
                        ((Number) stats.get("avgWaitTime")).doubleValue() : null);
                } else {
                    // 缓存未命中，查询数据库并写入缓存
                    Double successRate = waitlistMapper.calculateSuccessRate(
                        schedule.getDoctorId(),
                        schedule.getTimeSlot(),
                        7  // 统计最近7天
                    );
                    Double avgWaitTime = waitlistMapper.calculateAvgWaitTime(
                        schedule.getDoctorId(),
                        schedule.getTimeSlot(),
                        7  // 统计最近7天
                    );
                    
                    dto.setSuccessRate(successRate != null ? successRate : 0.0);
                    dto.setAvgWaitTime(avgWaitTime);
                    
                    // 将统计数据写入Redis缓存
                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("successRate", dto.getSuccessRate());
                    stats.put("avgWaitTime", dto.getAvgWaitTime());
                    redisTemplate.opsForValue().set(statsKey, stats, 
                        java.time.Duration.ofSeconds(STATS_CACHE_EXPIRE_SECONDS));
                }
            } catch (Exception e) {
                // 如果统计计算失败，设置默认值
                dto.setSuccessRate(0.0);
                dto.setAvgWaitTime(null);
            }
        }
        return dto;
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
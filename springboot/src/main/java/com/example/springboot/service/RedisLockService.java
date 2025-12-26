package com.example.springboot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Redis分布式锁服务
 * 用于保护高并发场景下的号源操作
 */
@Service
public class RedisLockService {

    private static final Logger logger = LoggerFactory.getLogger(RedisLockService.class);

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final String SCHEDULE_LOCK_PREFIX = "schedule_lock:";

    /**
     * 尝试获取排班锁
     * @param scheduleId 排班ID
     * @param timeoutSeconds 锁超时时间（秒）
     * @return 是否获取成功
     */
    public boolean tryLock(Long scheduleId, int timeoutSeconds) {
        String lockKey = SCHEDULE_LOCK_PREFIX + scheduleId;
        try {
            Boolean result = stringRedisTemplate.opsForValue()
                .setIfAbsent(lockKey, "locked", timeoutSeconds, TimeUnit.SECONDS);
            return result != null && result;
        } catch (Exception e) {
            logger.error("获取分布式锁失败: scheduleId={}, error={}", scheduleId, e.getMessage());
            return false;
        }
    }

    /**
     * 释放排班锁
     * @param scheduleId 排班ID
     */
    public void unlock(Long scheduleId) {
        String lockKey = SCHEDULE_LOCK_PREFIX + scheduleId;
        try {
            stringRedisTemplate.delete(lockKey);
        } catch (Exception e) {
            logger.error("释放分布式锁失败: scheduleId={}, error={}", scheduleId, e.getMessage());
        }
    }

    /**
     * 使用分布式锁执行号源扣减操作
     * @param scheduleId 排班ID
     * @param operation 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(Long scheduleId, LockOperation<T> operation) {
        String lockKey = SCHEDULE_LOCK_PREFIX + scheduleId;
        boolean locked = false;

        try {
            // 尝试获取锁，最多等待2秒
            locked = tryLock(scheduleId, 30); // 30秒超时

            if (!locked) {
                logger.warn("获取排班锁失败，排班可能正在被其他请求处理: scheduleId={}", scheduleId);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }

            // 执行操作
            return operation.execute();

        } finally {
            if (locked) {
                unlock(scheduleId);
            }
        }
    }

    /**
     * 锁操作接口
     */
    @FunctionalInterface
    public interface LockOperation<T> {
        T execute();
    }
}

-- 数据库性能优化脚本
-- 为高并发场景优化索引和查询性能
-- 注意：请确保连接到 yi_dian_tong_2 数据库后再执行此脚本

-- 检查当前数据库
SELECT DATABASE() as current_database;

-- 1. 优化appointment表索引（预约查询性能关键）
-- 直接创建索引（如果已存在会报错并继续）
CREATE INDEX idx_appointment_patient_schedule ON appointment (patient_id, schedule_id);
CREATE INDEX idx_appointment_schedule_status ON appointment (schedule_id, status);
CREATE INDEX idx_appointment_patient_created ON appointment (patient_id, created_at DESC);

-- 2. 优化schedule表索引（号源查询性能关键）
CREATE INDEX idx_schedule_doctor_date_time ON schedule (doctor_id, schedule_date, time_slot);
CREATE INDEX idx_schedule_available_slots ON schedule (available_slots);

-- 3. 优化waitlist表索引（候补队列性能关键）
CREATE INDEX idx_waitlist_schedule_join ON waitlist (schedule_id, join_time);
CREATE INDEX idx_waitlist_patient_schedule ON waitlist (patient_id, schedule_id);

-- 4. 优化patient表索引（患者查询性能）
CREATE INDEX idx_patient_user_identity ON patient (user_id, identity_number);

-- 5. 分析当前索引使用情况
-- 执行以下查询来检查索引使用情况：
-- SHOW INDEX FROM appointment;
-- SHOW INDEX FROM schedule;
-- SHOW INDEX FROM waitlist;

-- 7. 数据库参数优化（在my.cnf或my.ini中配置）
/*
[mysqld]
# 连接池设置
max_connections = 200
wait_timeout = 28800
interactive_timeout = 28800

# 缓冲区设置
innodb_buffer_pool_size = 1G  # 根据服务器内存调整
innodb_log_file_size = 256M
innodb_log_buffer_size = 16M

# 查询缓存（MySQL 5.7及以下版本）
query_cache_size = 64M
query_cache_type = ON
query_cache_limit = 2M

# 并发设置
innodb_thread_concurrency = 16
max_prepared_stmt_count = 128000

# 临时表设置
tmp_table_size = 128M
max_heap_table_size = 128M
*/

-- 8. 创建性能监控视图
CREATE OR REPLACE VIEW performance_monitor AS
SELECT
    'appointment_count' as metric,
    COUNT(*) as value
FROM appointment
WHERE created_at >= DATE_SUB(NOW(), INTERVAL 1 HOUR)

UNION ALL

SELECT
    'concurrent_connections' as metric,
    COUNT(*) as value
FROM information_schema.processlist
WHERE command != 'Sleep'

UNION ALL

SELECT
    'active_transactions' as metric,
    COUNT(*) as value
FROM information_schema.innodb_trx;

-- 6. 数据一致性检查
-- 检查排班号源与预约数量的一致性

-- 创建号源一致性检查视图
CREATE OR REPLACE VIEW slot_consistency_check AS
SELECT
    s.id as schedule_id,
    s.doctor_id,
    s.schedule_date,
    s.time_slot,
    s.total_slots,
    s.available_slots,
    COUNT(a.id) as actual_appointments,
    (s.total_slots - s.available_slots) as expected_appointments,
    CASE
        WHEN (s.total_slots - s.available_slots) = COUNT(a.id) THEN 'CONSISTENT'
        WHEN (s.total_slots - s.available_slots) > COUNT(a.id) THEN 'UNDER_BOOKED'
        WHEN (s.total_slots - s.available_slots) < COUNT(a.id) THEN 'OVER_BOOKED'
        ELSE 'INCONSISTENT'
    END as consistency_status,
    CASE
        WHEN (s.total_slots - s.available_slots) != COUNT(a.id) THEN
            ABS((s.total_slots - s.available_slots) - COUNT(a.id))
        ELSE 0
    END as inconsistency_count
FROM schedule s
LEFT JOIN appointment a ON s.id = a.schedule_id AND a.status IN ('scheduled', 'completed')
GROUP BY s.id, s.doctor_id, s.schedule_date, s.time_slot, s.total_slots, s.available_slots;

-- 显示不一致的排班
SELECT * FROM slot_consistency_check
WHERE consistency_status != 'CONSISTENT'
ORDER BY inconsistency_count DESC;

-- 统计一致性概况
SELECT
    consistency_status,
    COUNT(*) as count,
    SUM(inconsistency_count) as total_inconsistencies
FROM slot_consistency_check
GROUP BY consistency_status;

-- 检查超卖情况（实际预约数超过总号源数）
SELECT
    s.id as schedule_id,
    s.doctor_id,
    s.schedule_date,
    s.time_slot,
    s.total_slots,
    COUNT(a.id) as actual_appointments,
    (COUNT(a.id) - s.total_slots) as overbooked_count
FROM schedule s
LEFT JOIN appointment a ON s.id = a.schedule_id AND a.status IN ('scheduled', 'completed')
GROUP BY s.id, s.doctor_id, s.schedule_date, s.time_slot, s.total_slots
HAVING COUNT(a.id) > s.total_slots;

-- 检查数据完整性问题
-- 查找没有对应排班的预约
SELECT
    a.id as appointment_id,
    a.patient_id,
    a.schedule_id,
    a.appointment_time,
    a.status
FROM appointment a
LEFT JOIN schedule s ON a.schedule_id = s.id
WHERE s.id IS NULL;

-- 查找没有对应患者的预约
SELECT
    a.id as appointment_id,
    a.patient_id,
    a.schedule_id,
    a.appointment_time,
    a.status
FROM appointment a
LEFT JOIN patient p ON a.patient_id = p.id
WHERE p.id IS NULL;

-- 查找没有对应医生的预约
SELECT
    a.id as appointment_id,
    a.doctor_id,
    a.schedule_id,
    a.appointment_time,
    a.status
FROM appointment a
LEFT JOIN doctor d ON a.doctor_id = d.id
WHERE d.id IS NULL;

-- 9. 高级查询性能优化

-- 添加更多复合索引以优化复杂查询
CREATE INDEX idx_appointment_doctor_date ON appointment (doctor_id, appointment_time DESC);
CREATE INDEX idx_appointment_status_time ON appointment (status, created_at DESC);
CREATE INDEX idx_schedule_date_slots ON schedule (schedule_date, available_slots, total_slots);
CREATE INDEX idx_doctor_clinic_dept ON doctor (clinic_id, department_id, title);
CREATE INDEX idx_clinic_department ON clinic (department_id);

-- 10. 优化频繁查询 - 预约统计查询
-- 创建预约统计缓存表（可选，用于高频统计查询）
CREATE TABLE IF NOT EXISTS appointment_stats_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    date_key DATE NOT NULL,
    doctor_id BIGINT,
    department_id BIGINT,
    total_appointments INT DEFAULT 0,
    completed_appointments INT DEFAULT 0,
    cancelled_appointments INT DEFAULT 0,
    no_show_appointments INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_stats_date_doctor (date_key, doctor_id),
    KEY idx_date_dept (date_key, department_id)
);

-- 11. 分页查询优化 - 确保所有列表查询都有合适的LIMIT
-- 优化预约列表查询，避免全表扫描
DELIMITER //

-- 创建存储过程来刷新预约统计缓存
CREATE PROCEDURE refresh_appointment_stats(IN target_date DATE)
BEGIN
    -- 删除旧的统计数据
    DELETE FROM appointment_stats_cache
    WHERE date_key = target_date;

    -- 插入新的统计数据
    INSERT INTO appointment_stats_cache (
        date_key, doctor_id, department_id,
        total_appointments, completed_appointments,
        cancelled_appointments, no_show_appointments
    )
    SELECT
        DATE(a.appointment_time) as date_key,
        a.doctor_id,
        d.department_id,
        COUNT(*) as total_appointments,
        SUM(CASE WHEN a.status = 'completed' THEN 1 ELSE 0 END) as completed_appointments,
        SUM(CASE WHEN a.status = 'cancelled' THEN 1 ELSE 0 END) as cancelled_appointments,
        SUM(CASE WHEN a.status = 'no_show' THEN 1 ELSE 0 END) as no_show_appointments
    FROM appointment a
    LEFT JOIN doctor d ON a.doctor_id = d.id
    WHERE DATE(a.appointment_time) = target_date
    GROUP BY DATE(a.appointment_time), a.doctor_id, d.department_id;
END //

DELIMITER ;

-- 12. 查询优化建议 - 重构复杂查询

-- 优化搜索可用时段查询（添加索引提示）
-- 注意：这个需要在应用层实现，或者使用索引提示

-- 13. 缓存策略优化
-- 为频繁查询的结果添加缓存
-- 以下查询建议在Redis中缓存：

-- 热门医生查询缓存
CREATE OR REPLACE VIEW popular_doctors AS
SELECT
    d.id,
    d.name,
    d.title,
    d.specialty,
    COUNT(a.id) as appointment_count,
    AVG(CASE WHEN a.status = 'completed' THEN 5 ELSE 0 END) as rating
FROM doctor d
LEFT JOIN appointment a ON d.id = a.doctor_id
WHERE a.created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY)
GROUP BY d.id, d.name, d.title, d.specialty
ORDER BY appointment_count DESC
LIMIT 20;

-- 科室统计缓存视图
CREATE OR REPLACE VIEW department_stats AS
SELECT
    dept.id as department_id,
    dept.name as department_name,
    COUNT(DISTINCT d.id) as doctor_count,
    COUNT(DISTINCT s.id) as schedule_count,
    SUM(s.total_slots) as total_slots,
    SUM(s.available_slots) as available_slots,
    COUNT(a.id) as appointment_count
FROM department dept
LEFT JOIN clinic c ON dept.id = c.department_id
LEFT JOIN doctor d ON c.id = d.clinic_id
LEFT JOIN schedule s ON d.id = s.doctor_id
LEFT JOIN appointment a ON s.id = a.schedule_id
WHERE s.schedule_date >= CURDATE()
GROUP BY dept.id, dept.name;

-- 14. 数据库连接池优化参数
/*
在 application.yml 中添加以下配置：

spring:
  datasource:
    hikari:
      # 连接池大小优化
      maximum-pool-size: 50  # 根据并发量调整
      minimum-idle: 10
      idle-timeout: 600000  # 10分钟
      max-lifetime: 1800000  # 30分钟
      connection-timeout: 30000  # 30秒

      # 性能优化
      leak-detection-threshold: 60000  # 连接泄露检测
      validation-timeout: 5000
      maximum-pool-size: 50
*/

-- 15. Redis缓存配置建议
/*
# 在 application.yml 中添加缓存配置：

spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000  # 1小时

  data:
    redis:
      # 连接池配置
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 3000ms
*/

-- 16. 慢查询分析视图
CREATE OR REPLACE VIEW slow_queries AS
SELECT
    digest_text as sql_text,
    count_star as exec_count,
    avg_timer_wait/1000000000 as avg_time_sec,
    max_timer_wait/1000000000 as max_time_sec,
    last_seen
FROM performance_schema.events_statements_summary_by_digest
WHERE avg_timer_wait > 1000000000  -- 超过1秒的查询
ORDER BY avg_timer_wait DESC
LIMIT 20;

-- 17. 查询性能监控
CREATE OR REPLACE VIEW query_performance_monitor AS
SELECT
    'total_queries' as metric,
    COUNT(*) as value
FROM performance_schema.events_statements_summary_by_digest

UNION ALL

SELECT
    'slow_queries' as metric,
    COUNT(*) as value
FROM performance_schema.events_statements_summary_by_digest
WHERE avg_timer_wait > 5000000000  -- 超过5秒

UNION ALL

SELECT
    'avg_query_time_ms' as metric,
    AVG(avg_timer_wait/1000000) as value
FROM performance_schema.events_statements_summary_by_digest

UNION ALL

SELECT
    'max_query_time_ms' as metric,
    MAX(max_timer_wait/1000000) as value
FROM performance_schema.events_statements_summary_by_digest;

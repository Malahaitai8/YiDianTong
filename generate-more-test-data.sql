-- 补充更多测试数据，确保报表统计有足够的数据显示

-- 1. 为更多排班创建预约记录（确保时间序列数据完整）
INSERT INTO `appointment` (
    `patient_id`, 
    `doctor_id`, 
    `schedule_id`, 
    `appointment_time`, 
    `status`, 
    `fee`, 
    `actual_fee`, 
    `created_at`
)
SELECT 
    (SELECT id FROM patient ORDER BY RAND() LIMIT 1) AS patient_id,
    s.doctor_id,
    s.id AS schedule_id,
    CASE 
        WHEN s.time_slot = 'morning' THEN CONCAT(s.schedule_date, ' 09:00:00')
        WHEN s.time_slot = 'afternoon' THEN CONCAT(s.schedule_date, ' 14:00:00')
        WHEN s.time_slot = 'evening' THEN CONCAT(s.schedule_date, ' 18:00:00')
        ELSE CONCAT(s.schedule_date, ' 10:00:00')
    END AS appointment_time,
    CASE 
        WHEN (s.id % 10) < 7 THEN 'completed'
        WHEN (s.id % 10) < 9 THEN 'scheduled'
        ELSE 'cancelled'
    END AS status,
    CASE 
        WHEN s.slot_type = 'expert' THEN 20.00
        WHEN s.slot_type = 'vip' THEN 50.00
        ELSE 10.00
    END AS fee,
    CASE 
        WHEN s.slot_type = 'expert' THEN 16.00
        WHEN s.slot_type = 'vip' THEN 40.00
        ELSE 8.00
    END AS actual_fee,
    DATE_SUB(s.schedule_date, INTERVAL (1 + (s.id % 2)) DAY) AS created_at
FROM schedule s
WHERE s.schedule_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND CURDATE()
AND s.available_slots > 0  -- 选择还有号源的排班
AND s.available_slots < s.total_slots  -- 但已经有一些预约
AND NOT EXISTS (
    SELECT 1 FROM appointment a WHERE a.schedule_id = s.id
)
LIMIT 100;

-- 2. 更新排班的可用号源
UPDATE schedule s
SET s.available_slots = GREATEST(0, s.total_slots - (
    SELECT COUNT(*) 
    FROM appointment a 
    WHERE a.schedule_id = s.id 
    AND a.status != 'cancelled'
))
WHERE s.schedule_date BETWEEN DATE_SUB(CURDATE(), INTERVAL 30 DAY) AND CURDATE();

-- 3. 验证数据
SELECT 
    'Total Appointments' as metric,
    COUNT(*) as value
FROM appointment
UNION ALL
SELECT 
    'Completed',
    COUNT(*)
FROM appointment
WHERE status = 'completed'
UNION ALL
SELECT 
    'Cancelled',
    COUNT(*)
FROM appointment
WHERE status = 'cancelled'
UNION ALL
SELECT 
    'Total Revenue',
    SUM(actual_fee)
FROM appointment
WHERE status IN ('completed', 'scheduled');


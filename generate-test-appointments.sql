-- 为报表统计生成测试预约数据
-- 生成最近30天的预约记录，包含不同状态、不同科室、不同号别

-- 设置变量
SET @start_date = DATE_SUB(CURDATE(), INTERVAL 30 DAY);
SET @end_date = CURDATE();

-- 获取可用的患者ID（前10个）
SET @patient_ids = (SELECT GROUP_CONCAT(id) FROM (SELECT id FROM patient LIMIT 10) AS p);

-- 生成预约记录
-- 状态分布：60% completed, 20% scheduled, 15% cancelled, 5% no_show

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
    -- 随机选择患者（从1-10中循环）
    (SELECT id FROM patient ORDER BY RAND() LIMIT 1) AS patient_id,
    s.doctor_id,
    s.id AS schedule_id,
    -- 根据排班日期和时间段计算预约时间
    CASE 
        WHEN s.time_slot = 'morning' THEN CONCAT(s.schedule_date, ' 09:00:00')
        WHEN s.time_slot = 'afternoon' THEN CONCAT(s.schedule_date, ' 14:00:00')
        WHEN s.time_slot = 'evening' THEN CONCAT(s.schedule_date, ' 18:00:00')
        ELSE CONCAT(s.schedule_date, ' 10:00:00')
    END AS appointment_time,
    -- 随机分配状态（60% completed, 20% scheduled, 15% cancelled, 5% no_show）
    CASE 
        WHEN (s.id % 10) < 6 THEN 'completed'
        WHEN (s.id % 10) < 8 THEN 'scheduled'
        WHEN (s.id % 10) < 9 THEN 'cancelled'
        ELSE 'no_show'
    END AS status,
    -- 根据号别设置挂号费
    CASE 
        WHEN s.slot_type = 'expert' THEN 20.00
        WHEN s.slot_type = 'vip' THEN 50.00
        ELSE 10.00
    END AS fee,
    -- 实际费用（假设有报销，实际费用为原始费用的80%）
    CASE 
        WHEN s.slot_type = 'expert' THEN 16.00
        WHEN s.slot_type = 'vip' THEN 40.00
        ELSE 8.00
    END AS actual_fee,
    -- 创建时间（预约日期前1-3天）
    DATE_SUB(s.schedule_date, INTERVAL (1 + (s.id % 3)) DAY) AS created_at
FROM schedule s
WHERE s.schedule_date BETWEEN @start_date AND @end_date
AND s.available_slots < s.total_slots  -- 只选择有预约的排班
AND NOT EXISTS (
    -- 避免重复插入（如果已有预约记录）
    SELECT 1 FROM appointment a WHERE a.schedule_id = s.id
)
LIMIT 200;  -- 生成200条预约记录

-- 更新排班的可用号源（确保数据一致性）
UPDATE schedule s
SET s.available_slots = s.total_slots - (
    SELECT COUNT(*) 
    FROM appointment a 
    WHERE a.schedule_id = s.id 
    AND a.status != 'cancelled'
)
WHERE s.schedule_date BETWEEN @start_date AND @end_date;

-- 查询生成的预约统计
SELECT 
    status,
    COUNT(*) as count,
    SUM(fee) as total_fee,
    SUM(actual_fee) as total_actual_fee
FROM appointment
WHERE created_at >= @start_date
GROUP BY status;

-- 查询按日期分布的预约
SELECT 
    DATE(appointment_time) as date,
    COUNT(*) as total_count,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled,
    SUM(CASE WHEN status = 'no_show' THEN 1 ELSE 0 END) as no_show,
    SUM(actual_fee) as total_revenue
FROM appointment
WHERE appointment_time >= @start_date
GROUP BY DATE(appointment_time)
ORDER BY date DESC
LIMIT 30;






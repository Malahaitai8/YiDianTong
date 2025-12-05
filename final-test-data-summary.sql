-- 最终测试数据总结和验证

-- 查看总体统计
SELECT 
    'Overview Statistics' as category,
    '' as detail,
    '' as value
UNION ALL
SELECT 
    'Total Appointments',
    '',
    CAST(COUNT(*) AS CHAR)
FROM appointment
UNION ALL
SELECT 
    'Completed',
    '',
    CAST(COUNT(*) AS CHAR)
FROM appointment
WHERE status = 'completed'
UNION ALL
SELECT 
    'Cancelled',
    '',
    CAST(COUNT(*) AS CHAR)
FROM appointment
WHERE status = 'cancelled'
UNION ALL
SELECT 
    'No Show',
    '',
    CAST(COUNT(*) AS CHAR)
FROM appointment
WHERE status = 'no_show'
UNION ALL
SELECT 
    'Total Revenue',
    '',
    CAST(SUM(actual_fee) AS CHAR)
FROM appointment
WHERE status IN ('completed', 'scheduled');

-- 查看按日期分布的预约（最近30天）
SELECT 
    DATE(appointment_time) as date,
    COUNT(*) as total,
    SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed,
    SUM(CASE WHEN status = 'cancelled' THEN 1 ELSE 0 END) as cancelled,
    SUM(actual_fee) as revenue
FROM appointment
WHERE appointment_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DATE(appointment_time)
ORDER BY date DESC;






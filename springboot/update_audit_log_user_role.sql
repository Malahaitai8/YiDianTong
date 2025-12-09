-- 更新审计日志表中的用户角色字段
-- 根据用户名从user表中查找对应的角色并更新

UPDATE audit_log al
INNER JOIN user u ON al.username = u.username
SET al.user_role = u.role
WHERE al.user_role IS NULL;

-- 查看更新结果
SELECT 
    COUNT(*) as total_records,
    SUM(CASE WHEN user_role IS NOT NULL THEN 1 ELSE 0 END) as records_with_role,
    SUM(CASE WHEN user_role IS NULL THEN 1 ELSE 0 END) as records_without_role
FROM audit_log;

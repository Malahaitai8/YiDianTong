-- ============================================
-- 医点通医院预约挂号系统 - Bug修复SQL语句
-- 创建时间: 2024
-- 说明: 修复系统测试报告中发现的bug
-- ============================================

-- ============================================
-- 1. 手机号唯一性约束（必须添加）
-- Bug编号: BUG-PAT-01, BUG-PAT-02
-- 说明: 在数据库层面添加唯一约束，防止重复手机号
--       这是修复方案中明确要求的数据库层修改
-- ============================================

-- 步骤1: 检查是否有重复手机号
SELECT phone_number, COUNT(*) as count 
FROM patient 
WHERE phone_number IS NOT NULL 
GROUP BY phone_number 
HAVING COUNT(*) > 1;

-- 步骤2: 如果存在重复数据，需要先处理重复数据
-- 建议：保留ID最小的记录，删除或更新其他的
-- 示例（请根据实际情况调整）：
-- UPDATE patient p1
-- INNER JOIN (
--     SELECT phone_number, MIN(id) as min_id
--     FROM patient
--     WHERE phone_number IS NOT NULL
--     GROUP BY phone_number
--     HAVING COUNT(*) > 1
-- ) p2 ON p1.phone_number = p2.phone_number AND p1.id != p2.min_id
-- SET p1.phone_number = NULL;  -- 或者删除这些记录

-- 步骤3: 添加唯一索引（在确认无重复数据后执行）
ALTER TABLE patient ADD UNIQUE INDEX uk_phone_number (phone_number);

-- ============================================
-- 说明：
-- 1. 数据库唯一索引是修复方案中明确要求的
-- 2. 代码层面已实现校验，但无法防止并发情况下的重复插入
-- 3. 数据库唯一索引是最后一道防线，确保数据完整性
-- 4. 代码中已捕获DuplicateKeyException异常，会返回友好的错误提示
-- ============================================


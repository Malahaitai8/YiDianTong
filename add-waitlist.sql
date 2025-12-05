-- 为 scheduleId 183-190 的每个排班添加两个候补
-- 注意：需要先查询可用的患者ID，这里使用示例患者ID（请根据实际情况修改）

-- 假设我们使用患者ID 1 和 2（请根据实际数据库中的患者ID修改）
-- 如果需要使用其他患者，请先查询：SELECT id FROM patient LIMIT 10;

-- 为每个排班添加两个候补记录
-- ScheduleId 183
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(183, 1, NOW(), 'WAITING'),
(183, 2, NOW(), 'WAITING');

-- ScheduleId 184
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(184, 1, NOW(), 'WAITING'),
(184, 2, NOW(), 'WAITING');

-- ScheduleId 185
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(185, 1, NOW(), 'WAITING'),
(185, 2, NOW(), 'WAITING');

-- ScheduleId 186
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(186, 1, NOW(), 'WAITING'),
(186, 2, NOW(), 'WAITING');

-- ScheduleId 187
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(187, 1, NOW(), 'WAITING'),
(187, 2, NOW(), 'WAITING');

-- ScheduleId 188
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(188, 1, NOW(), 'WAITING'),
(188, 2, NOW(), 'WAITING');

-- ScheduleId 189
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(189, 1, NOW(), 'WAITING'),
(189, 2, NOW(), 'WAITING');

-- ScheduleId 190
INSERT INTO `waitlist` (`schedule_id`, `patient_id`, `join_time`, `status`) VALUES
(190, 1, NOW(), 'WAITING'),
(190, 2, NOW(), 'WAITING');

-- 查询刚插入的候补记录ID（用于后续添加到Redis）
SELECT id, schedule_id, patient_id, join_time, status 
FROM waitlist 
WHERE schedule_id BETWEEN 183 AND 190 
ORDER BY schedule_id, id;






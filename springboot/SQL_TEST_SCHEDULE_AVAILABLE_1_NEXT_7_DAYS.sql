-- 批量生成未来7天（包含今天）可用号源为1的排班数据
-- 说明：
-- 1) 使用了 MySQL 的 ON DUPLICATE KEY UPDATE，若该医生在该日该时段已有排班，则更新为余量=1
-- 2) 选用 doctor_id=1 与 doctor_id=5（请确保这两个医生ID在库中存在；如需调整，直接替换ID即可）
-- 3) 前端仅展示 morning/afternoon 两个时间段；此脚本两者都会生成
-- 4) slot_type 兼顾 normal/expert，total_slots 给出示例值（10 或 20）

START TRANSACTION;

-- doctor_id = 1，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(1, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'normal', 20, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'normal', 20, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'normal', 20, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'normal', 20, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'normal', 20, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'normal', 20, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'expert', 10, 1),
(1, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'normal', 20, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 5，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(5, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'expert', 10, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'expert', 10, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'expert', 10, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'expert', 10, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'expert', 10, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'expert', 10, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'normal', 20, 1),
(5, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'expert', 10, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);


-- doctor_id = 2，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(2, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'expert', 10, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'expert', 10, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'expert', 10, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'expert', 10, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'expert', 10, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'expert', 10, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'normal', 20, 1),
(2, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'expert', 10, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 3，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(3, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'normal', 20, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'normal', 20, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'normal', 20, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'normal', 20, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'normal', 20, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'normal', 20, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'expert', 10, 1),
(3, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'normal', 20, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 4，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(4, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'normal', 20, 1),
(4, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'normal', 20, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 6，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(6, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'expert', 10, 1),
(6, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'expert', 10, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 7，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(7, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'expert', 10, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'expert', 10, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'expert', 10, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'expert', 10, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'expert', 10, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'expert', 10, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'normal', 20, 1),
(7, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'expert', 10, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 8，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(8, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'normal', 20, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'normal', 20, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'normal', 20, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'normal', 20, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'normal', 20, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'normal', 20, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'expert', 10, 1),
(8, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'normal', 20, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 9，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(9, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'expert', 10, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'expert', 10, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'expert', 10, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'expert', 10, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'expert', 10, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'expert', 10, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'normal', 20, 1),
(9, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'expert', 10, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

-- doctor_id = 10，未来7天，上午/下午
INSERT INTO schedule (doctor_id, schedule_date, time_slot, slot_type, total_slots, available_slots) VALUES
(10, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 0 DAY), 'afternoon', 'normal', 20, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'afternoon', 'normal', 20, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 2 DAY), 'afternoon', 'normal', 20, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'afternoon', 'normal', 20, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 4 DAY), 'afternoon', 'normal', 20, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 5 DAY), 'afternoon', 'normal', 20, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'morning',   'expert', 10, 1),
(10, DATE_ADD(CURDATE(), INTERVAL 6 DAY), 'afternoon', 'normal', 20, 1)
ON DUPLICATE KEY UPDATE
  available_slots = 1,
  total_slots = VALUES(total_slots),
  slot_type = VALUES(slot_type);

COMMIT;


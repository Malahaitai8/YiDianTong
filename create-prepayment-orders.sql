-- 为候补记录创建预支付订单
-- 注意：需要先执行 add-waitlist.sql 创建候补记录

-- 设置默认挂号费（普通号 10 元，专家号 20 元，特需号 50 元）
-- 这里使用普通号价格 10 元作为默认值

-- 为每个候补记录创建预支付订单
-- 订单号格式：WL{waitlist_id}_{timestamp}
-- 状态设为 PAID（已支付），这样候补才能自动转为预约

INSERT INTO `prepayment_order` (
    `order_no`, 
    `patient_id`, 
    `schedule_id`, 
    `waitlist_id`, 
    `order_type`, 
    `original_fee`, 
    `actual_fee`, 
    `paid_amount`, 
    `status`, 
    `payment_method`, 
    `payment_time`, 
    `expire_time`
) 
SELECT 
    CONCAT('WL', w.id, '_', UNIX_TIMESTAMP(NOW())) AS order_no,
    w.patient_id,
    w.schedule_id,
    w.id AS waitlist_id,
    'WAITLIST' AS order_type,
    -- 根据排班的 slot_type 设置挂号费（这里简化处理，统一使用 10 元）
    CASE 
        WHEN s.slot_type = 'expert' THEN 20.00
        WHEN s.slot_type = 'vip' THEN 50.00
        ELSE 10.00
    END AS original_fee,
    CASE 
        WHEN s.slot_type = 'expert' THEN 20.00
        WHEN s.slot_type = 'vip' THEN 50.00
        ELSE 10.00
    END AS actual_fee,
    CASE 
        WHEN s.slot_type = 'expert' THEN 20.00
        WHEN s.slot_type = 'vip' THEN 50.00
        ELSE 10.00
    END AS paid_amount,
    'PAID' AS status,
    'BALANCE' AS payment_method,
    NOW() AS payment_time,
    DATE_ADD(NOW(), INTERVAL 7 DAY) AS expire_time
FROM waitlist w
INNER JOIN schedule s ON w.schedule_id = s.id
WHERE w.schedule_id BETWEEN 183 AND 190
AND w.status = 'WAITING'
AND NOT EXISTS (
    SELECT 1 FROM prepayment_order po WHERE po.waitlist_id = w.id
);

-- 查询刚创建的预支付订单
SELECT 
    po.id,
    po.order_no,
    po.waitlist_id,
    po.schedule_id,
    po.patient_id,
    po.original_fee,
    po.actual_fee,
    po.status,
    po.payment_time
FROM prepayment_order po
WHERE po.waitlist_id IN (
    SELECT id FROM waitlist WHERE schedule_id BETWEEN 183 AND 190
)
ORDER BY po.waitlist_id;






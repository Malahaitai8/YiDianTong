-- 1. 创建科室表 (department)
CREATE TABLE `department` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '科室唯一ID',
    `name` VARCHAR(100) NOT NULL COMMENT '科室名称',
    `description` TEXT NULL COMMENT '科室的详细介绍',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_department_name` (`name`)
) COMMENT='一级科室信息表';

-- 2. 创建门诊表 (clinic)
CREATE TABLE `clinic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '门诊唯一ID',
    `department_id` BIGINT NOT NULL COMMENT '所属科室的ID',
    `name` VARCHAR(100) NOT NULL COMMENT '门诊名称',
    `description` TEXT NULL COMMENT '门诊的详细介绍',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY `uk_clinic_dept_name` (`department_id`, `name`)
) COMMENT='二级门诊信息表';

-- 3. 统一用户表 (user)
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
    `username` VARCHAR(50) NOT NULL COMMENT '登录用户名/凭证',
    `password` VARCHAR(255) NOT NULL COMMENT '加密后的密码',
    `role` VARCHAR(20) NOT NULL COMMENT '身份角色 (patient, doctor, admin)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '账户状态 (active, inactive, pending_approval)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_username` (`username`)
) COMMENT='统一用户登录与身份信息表';


-- 4. 医生表 (doctor)
CREATE TABLE `doctor` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '医生唯一ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID (外键关联user表)',
    `clinic_id` BIGINT NOT NULL COMMENT '所属门诊的ID',
    `name` VARCHAR(50) NOT NULL COMMENT '医生姓名',
    `title` VARCHAR(50) NULL COMMENT '医生职称',
    `specialty` TEXT NULL COMMENT '擅长领域',
    `bio` TEXT NULL COMMENT '个人简介',
    PRIMARY KEY (`id`),
    -- 医生信息与用户账户一一对应
    UNIQUE KEY `uk_doctor_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`clinic_id`) REFERENCES `clinic`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) COMMENT='医生信息表';


-- 5. 患者表 (patient)
CREATE TABLE `patient` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '患者唯一ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID (外键关联user表)',
    `name` VARCHAR(50) NULL COMMENT '患者真实姓名（注册时为空，认证时填写）',
    `specific_role` VARCHAR(20) NULL COMMENT '具体角色 (student/teacher/outsider，注册时为空，认证时根据白名单确定)',
    `id_status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '身份认证状态 (pending/verified)',
    `phone_number` VARCHAR(20) NULL COMMENT '手机号码',
    `id_card_number` VARCHAR(255) NULL COMMENT '身份证号 (需加密存储，认证时填写)',
    `identity_number` VARCHAR(50) NULL COMMENT '学号或工号（认证时填写）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_patient_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_patient_identity_number` (`identity_number`)
) COMMENT='患者(师生)信息表';


-- 6. 白名单表 (whitelist)
CREATE TABLE `whitelist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '白名单唯一ID',
    `identity_number` VARCHAR(50) NOT NULL COMMENT '学号或工号',
    `role_type` VARCHAR(20) NOT NULL COMMENT '角色类型 (student/teacher/outsider)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'active' COMMENT '状态 (active/inactive)',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_whitelist_identity_number` (`identity_number`),
    INDEX `idx_whitelist_role_type` (`role_type`),
    INDEX `idx_whitelist_status` (`status`)
) COMMENT='身份认证白名单表（学号/工号）';

-- 7. 管理员表 (admin)
CREATE TABLE `admin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '管理员唯一ID',
    `user_id` BIGINT NOT NULL COMMENT '关联的用户ID (外键关联user表)',
    `name` VARCHAR(50) NOT NULL COMMENT '管理员姓名',
    `admin_role` VARCHAR(30) NULL COMMENT '管理员具体角色 (如：超级管理员)',
    PRIMARY KEY (`id`),
    -- 管理员信息与用户账户一一对应
    UNIQUE KEY `uk_admin_user_id` (`user_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE CASCADE ON UPDATE CASCADE
) COMMENT='管理员信息表';

-- 8. 创建排班表 (schedule)
CREATE TABLE `schedule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '排班唯一ID',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
    `schedule_date` DATE NOT NULL COMMENT '出诊日期',
    `time_slot` VARCHAR(20) NOT NULL COMMENT '时间段 (morning/afternoon)',
    `slot_type` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '号别 (normal, expert, vip)',
    `total_slots` INT NOT NULL COMMENT '总号源数',
    `available_slots` INT NOT NULL COMMENT '剩余号源数',
    PRIMARY KEY (`id`),
    -- 外键引用已更新为 doctor
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_doctor_schedule` (`doctor_id`, `schedule_date`, `time_slot`) -- 一个医生在一个时间段只能有一个排班
) COMMENT='医生排班表';

-- 9. 创建预约记录表 (appointment)
CREATE TABLE `appointment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约唯一ID',
    `patient_id` BIGINT NOT NULL COMMENT '患者ID (外键关联patient表)',
    `doctor_id` BIGINT NOT NULL COMMENT '医生ID',
    `schedule_id` BIGINT NOT NULL COMMENT '排班ID',
    `appointment_time` DATETIME NOT NULL COMMENT '预约成功后确定的就诊时间',
    `status` VARCHAR(20) NOT NULL DEFAULT 'scheduled' COMMENT '状态 (scheduled/cancelled/completed)',
    `fee` DECIMAL(10,2) NOT NULL COMMENT '原始挂号费',
    `actual_fee` DECIMAL(10,2) NOT NULL COMMENT '根据角色报销后的实际支付费用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    PRIMARY KEY (`id`),
    -- 外键引用已更新为 patient, doctor, schedule
    FOREIGN KEY (`patient_id`) REFERENCES `patient`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (`schedule_id`) REFERENCES `schedule`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) COMMENT='预约记录表';

-- 10. 创建候补队列表 (waitlist)
CREATE TABLE `waitlist` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '候补记录唯一ID',
    `schedule_id` BIGINT NOT NULL COMMENT '目标排班ID',
    `patient_id` BIGINT NOT NULL COMMENT '候补的患者ID',
    `join_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入候补队列的时间 (用于确定优先级)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'WAITING' COMMENT '候补状态 (WAITING, NOTIFIED, GRANTED, EXPIRED)',

    PRIMARY KEY (`id`),

    -- 确保一个患者不能为同一个排班重复加入候补
    UNIQUE KEY `uk_waitlist_unique` (`schedule_id`, `patient_id`),

    -- 外键关联到排班表 (schedule)
    FOREIGN KEY (`schedule_id`) REFERENCES `schedule`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    -- 外键关联到患者表 (patient)
    FOREIGN KEY (`patient_id`) REFERENCES `patient`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) COMMENT='预约候补队列记录表';

-- 11. 创建系统配置与规则表 (system_config)
CREATE TABLE `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置唯一ID',
    `key` VARCHAR(100) NOT NULL COMMENT '配置项键名 (如：STUDENT_REIMBURSEMENT_RATE)',
    `value` VARCHAR(500) NOT NULL COMMENT '配置项值',
    `description` VARCHAR(255) NULL COMMENT '配置项描述',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',

    PRIMARY KEY (`id`),
    -- 配置键名必须唯一
    UNIQUE KEY `uk_config_key` (`key`)
) COMMENT='系统全局配置与规则表';

-- 12. 插入系统配置初始数据
INSERT INTO `system_config` (`key`, `value`, `description`) VALUES
('STUDENT_REIMBURSEMENT_RATE', '0.95', '学生挂号费报销比例'),
('TEACHER_REIMBURSEMENT_RATE', '0.90', '教师挂号费报销比例'),
('CANCELLATION_FREE_HOURS', '24', '就诊前多久取消可免费退号，否则记录爽约（单位：小时）');

-- 13. 为预约表添加来源类型字段
ALTER TABLE `appointment` ADD COLUMN `source_type` VARCHAR(20) COMMENT '预约来源类型';

-- ========================================
-- 14. 排班规则表 (schedule_rule)
-- ========================================
CREATE TABLE `schedule_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则唯一ID',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '规则类型 (weekly/custom/template)',
    `doctor_id` BIGINT NULL COMMENT '关联医生ID (可为空表示科室规则)',
    `department_id` BIGINT NULL COMMENT '关联科室ID',
    `clinic_id` BIGINT NULL COMMENT '关联门诊ID',
    
    -- 时间相关配置
    `week_days` VARCHAR(50) NULL COMMENT '星期配置 (如: 1,3,5 表示周一三五)',
    `time_slots` VARCHAR(100) NULL COMMENT '时段配置 (如: morning,afternoon)',
    `start_date` DATE NULL COMMENT '规则生效开始日期',
    `end_date` DATE NULL COMMENT '规则生效结束日期',
    
    -- 号源配置
    `slot_type` VARCHAR(20) NOT NULL DEFAULT 'normal' COMMENT '号别类型 (normal/expert/vip)',
    `total_slots` INT NOT NULL COMMENT '每次排班的总号源数',
    
    -- 高级规则
    `max_daily_schedules` INT NULL COMMENT '每天最多排班次数',
    `max_continuous_days` INT NULL COMMENT '最多连续出诊天数',
    `skip_weekends` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否跳过周末',
    `skip_holidays` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否跳过节假日',
    
    -- 状态与优先级
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '规则状态 (ACTIVE/INACTIVE/EXPIRED)',
    `priority` INT NOT NULL DEFAULT 0 COMMENT '优先级 (数字越大优先级越高)',
    
    -- 描述与审计
    `description` TEXT NULL COMMENT '规则描述说明',
    `created_by` VARCHAR(50) NOT NULL COMMENT '创建人',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`department_id`) REFERENCES `department`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`clinic_id`) REFERENCES `clinic`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_rule_doctor` (`doctor_id`),
    INDEX `idx_rule_status` (`status`)
) COMMENT='排班规则配置表 - 管理员可设置自动化排班策略';

-- ========================================
-- 15. 申请记录表 (application_request)
-- ========================================
CREATE TABLE `application_request` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '申请唯一ID',
    `request_type` VARCHAR(50) NOT NULL COMMENT '申请类型 (SCHEDULE_CHANGE/INFO_UPDATE)',
    `applicant_id` BIGINT NOT NULL COMMENT '申请人用户ID',
    `applicant_role` VARCHAR(20) NOT NULL COMMENT '申请人角色 (doctor/admin)',
    
    -- 调班申请相关字段
    `schedule_id` BIGINT NULL COMMENT '原排班ID (调班申请时使用)',
    `change_type` VARCHAR(50) NULL COMMENT '调班类型 (CANCEL/RESCHEDULE/ADJUST_SLOTS)',
    `original_date` DATE NULL COMMENT '原出诊日期',
    `original_time_slot` VARCHAR(20) NULL COMMENT '原时间段',
    `new_date` DATE NULL COMMENT '新出诊日期 (改期时使用)',
    `new_time_slot` VARCHAR(20) NULL COMMENT '新时间段 (改期时使用)',
    `slot_adjustment` INT NULL COMMENT '号源调整数量 (正数增加/负数减少)',
    
    -- 信息修改申请相关字段
    `doctor_id` BIGINT NULL COMMENT '要修改信息的医生ID (信息修改申请时使用)',
    `field_name` VARCHAR(100) NULL COMMENT '要修改的字段名 (如: name/title/specialty/bio)',
    `old_value` TEXT NULL COMMENT '原值',
    `new_value` TEXT NULL COMMENT '新值',
    
    -- 申请状态与处理
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申请状态 (PENDING/APPROVED/REJECTED/CANCELLED)',
    `reason` TEXT NULL COMMENT '申请理由',
    `reject_reason` TEXT NULL COMMENT '拒绝原因',
    `reviewer_id` BIGINT NULL COMMENT '审核人用户ID',
    `reviewed_at` DATETIME NULL COMMENT '审核时间',
    
    -- 审计信息
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    
    PRIMARY KEY (`id`),
    FOREIGN KEY (`applicant_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (`schedule_id`) REFERENCES `schedule`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (`doctor_id`) REFERENCES `doctor`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (`reviewer_id`) REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_request_type` (`request_type`),
    INDEX `idx_request_status` (`status`),
    INDEX `idx_applicant` (`applicant_id`),
    INDEX `idx_doctor` (`doctor_id`)
) COMMENT='统一申请记录表 - 管理调班申请和医生信息修改申请';

-- 创建问题记录表 (question_log)
CREATE TABLE `question_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '问题记录唯一ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID (外键关联user表)',
    `question_content` TEXT NOT NULL COMMENT '问题内容',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',

    PRIMARY KEY (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `user`(`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) COMMENT='用户问题记录表 - 用于统计高频问题';
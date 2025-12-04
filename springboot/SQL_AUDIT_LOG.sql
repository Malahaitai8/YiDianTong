-- 审计日志表 (audit_log)
-- 用于记录系统所有关键操作的审计信息

CREATE TABLE `audit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志唯一ID',
    `user_id` BIGINT NULL COMMENT '操作人用户ID',
    `username` VARCHAR(50) NULL COMMENT '操作人用户名',
    `user_role` VARCHAR(20) NULL COMMENT '操作人角色 (patient, doctor, admin)',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型 (CREATE, UPDATE, DELETE, APPROVE, REJECT, QUERY)',
    `operation_module` VARCHAR(50) NULL COMMENT '操作模块 (SCHEDULE, APPOINTMENT, WAITLIST, AUDIT, USER, DOCTOR, DEPARTMENT)',
    `operation_desc` VARCHAR(255) NULL COMMENT '操作描述',
    `target_type` VARCHAR(50) NULL COMMENT '目标对象类型',
    `target_id` BIGINT NULL COMMENT '目标对象ID',
    `request_method` VARCHAR(10) NULL COMMENT 'HTTP请求方法 (GET, POST, PUT, DELETE, PATCH)',
    `request_url` VARCHAR(500) NULL COMMENT '请求URL',
    `request_params` TEXT NULL COMMENT '请求参数 (JSON格式)',
    `response_code` VARCHAR(10) NULL COMMENT 'HTTP响应状态码',
    `response_msg` VARCHAR(500) NULL COMMENT '响应消息',
    `ip_address` VARCHAR(50) NULL COMMENT '客户端IP地址',
    `user_agent` VARCHAR(500) NULL COMMENT 'User-Agent信息',
    `execution_time` BIGINT NULL COMMENT '执行耗时 (毫秒)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'SUCCESS' COMMENT '操作状态 (SUCCESS, FAILURE)',
    `error_message` TEXT NULL COMMENT '错误信息',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_username` (`username`),
    KEY `idx_operation_type` (`operation_type`),
    KEY `idx_operation_module` (`operation_module`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_target` (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志表';


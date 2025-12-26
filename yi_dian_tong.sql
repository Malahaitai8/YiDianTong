/*
 Navicat Premium Dump SQL

 Source Server         : 医点通
 Source Server Type    : MySQL
 Source Server Version : 80028 (8.0.28-231003)
 Source Host           : 120.46.206.75:3306
 Source Schema         : yi_dian_tong

 Target Server Type    : MySQL
 Target Server Version : 80028 (8.0.28-231003)
 File Encoding         : 65001

 Date: 25/12/2025 20:40:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员唯一ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID (外键关联user表)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '管理员姓名',
  `admin_role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '管理员具体角色 (如：超级管理员)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `admin_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for application_request
-- ----------------------------
DROP TABLE IF EXISTS `application_request`;
CREATE TABLE `application_request`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请唯一ID',
  `request_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请类型 (SCHEDULE_CHANGE/INFO_UPDATE)',
  `applicant_id` bigint NOT NULL COMMENT '申请人用户ID',
  `applicant_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '申请人角色 (doctor/admin)',
  `schedule_id` bigint NULL DEFAULT NULL COMMENT '原排班ID (调班申请时使用)',
  `change_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '调班类型 (CANCEL/RESCHEDULE/ADJUST_SLOTS)',
  `original_date` date NULL DEFAULT NULL COMMENT '原出诊日期',
  `original_time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '原时间段',
  `new_date` date NULL DEFAULT NULL COMMENT '新出诊日期 (改期时使用)',
  `new_time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '新时间段 (改期时使用)',
  `slot_adjustment` int NULL DEFAULT NULL COMMENT '号源调整数量 (正数增加/负数减少)',
  `doctor_id` bigint NULL DEFAULT NULL COMMENT '要修改信息的医生ID (信息修改申请时使用)',
  `field_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '要修改的字段名 (如: name/title/specialty/bio)',
  `old_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '原值',
  `new_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '新值',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '申请状态 (PENDING/APPROVED/REJECTED/CANCELLED)',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '申请理由',
  `reject_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '拒绝原因',
  `reviewer_id` bigint NULL DEFAULT NULL COMMENT '审核人用户ID',
  `reviewed_at` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `reviewer_id`(`reviewer_id` ASC) USING BTREE,
  INDEX `idx_request_type`(`request_type` ASC) USING BTREE,
  INDEX `idx_request_status`(`status` ASC) USING BTREE,
  INDEX `idx_applicant`(`applicant_id` ASC) USING BTREE,
  INDEX `idx_doctor`(`doctor_id` ASC) USING BTREE,
  CONSTRAINT `application_request_ibfk_1` FOREIGN KEY (`applicant_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `application_request_ibfk_2` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `application_request_ibfk_3` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `application_request_ibfk_4` FOREIGN KEY (`reviewer_id`) REFERENCES `user` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一申请记录表 - 管理调班申请和医生信息修改申请' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for appointment
-- ----------------------------
DROP TABLE IF EXISTS `appointment`;
CREATE TABLE `appointment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约唯一ID',
  `patient_id` bigint NOT NULL COMMENT '患者ID (外键关联patient表)',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `appointment_time` datetime NOT NULL COMMENT '预约成功后确定的就诊时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'scheduled' COMMENT '状态 (scheduled/cancelled/completed)',
  `fee` decimal(10, 2) NOT NULL COMMENT '原始挂号费',
  `actual_fee` decimal(10, 2) NOT NULL COMMENT '根据角色报销后的实际支付费用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `source_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预约来源类型',
  `reschedule_window_expires` datetime NULL DEFAULT NULL COMMENT '重新选择窗口到期时间（调班后用户可重新选择时段的截止时间）',
  `auto_assigned` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '是否为系统自动分配 (Y/N)',
  `original_schedule_id` bigint NULL DEFAULT NULL COMMENT '原排班ID（调班场景下记录原始排班，便于重新选择）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `doctor_id`(`doctor_id` ASC) USING BTREE,
  INDEX `schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `idx_reschedule_window_expires`(`reschedule_window_expires` ASC) USING BTREE,
  INDEX `idx_auto_assigned`(`auto_assigned` ASC) USING BTREE,
  INDEX `idx_original_schedule_id`(`original_schedule_id` ASC) USING BTREE,
  CONSTRAINT `appointment_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `appointment_ibfk_2` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `appointment_ibfk_3` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `appointment_ibfk_4` FOREIGN KEY (`original_schedule_id`) REFERENCES `schedule` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 661 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '审计日志唯一ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '操作用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户名（冗余字段，便于查询）',
  `user_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户角色 (patient/doctor/admin)',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型 (LOGIN/LOGOUT/CREATE/UPDATE/DELETE/QUERY/APPROVE/REJECT等)',
  `operation_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作模块 (AUTH/APPOINTMENT/WAITLIST/SCHEDULE/DOCTOR/PATIENT等)',
  `operation_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `target_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标对象类型 (APPOINTMENT/WAITLIST/SCHEDULE等)',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标对象ID',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP请求方法 (GET/POST/PUT/DELETE)',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数（JSON格式）',
  `response_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '响应状态码',
  `response_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应消息',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户端IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理（浏览器/客户端信息）',
  `execution_time` bigint NULL DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'SUCCESS' COMMENT '操作状态 (SUCCESS/FAILURE)',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息（失败时记录）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_operation_module`(`operation_module` ASC) USING BTREE,
  INDEX `idx_target_type_id`(`target_type` ASC, `target_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_user_role`(`user_role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 107 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统审计日志表 - 记录所有关键操作' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for clinic
-- ----------------------------
DROP TABLE IF EXISTS `clinic`;
CREATE TABLE `clinic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '门诊唯一ID',
  `department_id` bigint NOT NULL COMMENT '所属科室的ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '门诊名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '门诊的详细介绍',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_clinic_dept_name`(`department_id` ASC, `name` ASC) USING BTREE,
  CONSTRAINT `clinic_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '二级门诊信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '科室唯一ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '科室名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '科室的详细介绍',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_department_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '一级科室信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '医生唯一ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID (外键关联user表)',
  `clinic_id` bigint NOT NULL COMMENT '所属门诊的ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '医生姓名',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '医生职称',
  `specialty` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '擅长领域',
  `bio` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_user_id`(`user_id` ASC) USING BTREE,
  INDEX `clinic_id`(`clinic_id` ASC) USING BTREE,
  CONSTRAINT `doctor_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `doctor_ibfk_2` FOREIGN KEY (`clinic_id`) REFERENCES `clinic` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 41 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知记录唯一ID',
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `patient_id` bigint NULL DEFAULT NULL COMMENT '患者ID（冗余字段，便于查询）',
  `notification_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型 (WAITLIST_SUCCESS/APPOINTMENT_SUCCESS/APPOINTMENT_REMINDER/CANCEL等)',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `related_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联对象类型 (APPOINTMENT/WAITLIST等)',
  `related_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID',
  `channel` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WECHAT' COMMENT '通知渠道 (WECHAT/SMS/EMAIL/APP)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '通知状态 (PENDING/SENT/FAILED)',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `read_time` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '发送失败原因',
  `template_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信模板ID',
  `template_data` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板数据（JSON格式）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE,
  INDEX `idx_notification_type`(`notification_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  CONSTRAINT `notification_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `notification_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 670 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知记录表 - 记录所有通知发送记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '患者唯一ID',
  `user_id` bigint NOT NULL COMMENT '关联的用户ID (外键关联user表)',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '患者真实姓名（注册时为空，认证时填写）',
  `specific_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '具体角色 (student/teacher/outsider，注册时为空，认证时根据白名单确定)',
  `id_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '身份认证状态 (pending/verified)',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码',
  `id_card_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '身份证号 (需加密存储，认证时填写)',
  `identity_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学号或工号（认证时填写）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_patient_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_phone_number`(`phone_number` ASC) USING BTREE,
  INDEX `idx_patient_identity_number`(`identity_number` ASC) USING BTREE,
  CONSTRAINT `patient_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '患者(师生)信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for prepayment_order
-- ----------------------------
DROP TABLE IF EXISTS `prepayment_order`;
CREATE TABLE `prepayment_order`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预支付订单唯一ID',
  `order_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号（唯一）',
  `patient_id` bigint NOT NULL COMMENT '患者ID',
  `schedule_id` bigint NOT NULL COMMENT '排班ID',
  `waitlist_id` bigint NOT NULL COMMENT '候补记录ID',
  `order_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WAITLIST' COMMENT '订单类型 (WAITLIST-候补预支付)',
  `original_fee` decimal(10, 2) NOT NULL COMMENT '原始挂号费',
  `actual_fee` decimal(10, 2) NOT NULL COMMENT '实际支付费用（报销后）',
  `paid_amount` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '已支付金额',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT '订单状态 (PENDING-待支付, PAID-已支付, REFUNDED-已退款, CONSUMED-已消费, EXPIRED-已过期)',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式 (ALIPAY, WECHAT, BALANCE等)',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
  `expire_time` datetime NOT NULL COMMENT '订单过期时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `schedule_id`(`schedule_id` ASC) USING BTREE,
  INDEX `idx_patient_status`(`patient_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_waitlist_status`(`waitlist_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  CONSTRAINT `prepayment_order_ibfk_1` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `prepayment_order_ibfk_2` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `prepayment_order_ibfk_3` FOREIGN KEY (`waitlist_id`) REFERENCES `waitlist` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '候补预支付订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for question_log
-- ----------------------------
DROP TABLE IF EXISTS `question_log`;
CREATE TABLE `question_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '问题记录唯一ID',
  `user_id` bigint NOT NULL COMMENT '用户ID (外键关联user表)',
  `question_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `question_log_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 94 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户问题记录表 - 用于统计高频问题' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for schedule
-- ----------------------------
DROP TABLE IF EXISTS `schedule`;
CREATE TABLE `schedule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '排班唯一ID',
  `doctor_id` bigint NOT NULL COMMENT '医生ID',
  `schedule_date` date NOT NULL COMMENT '出诊日期',
  `time_slot` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '时间段 (morning/afternoon)',
  `slot_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'normal' COMMENT '号别 (normal, expert, vip)',
  `total_slots` int NOT NULL COMMENT '总号源数',
  `available_slots` int NOT NULL COMMENT '剩余号源数',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_doctor_schedule`(`doctor_id` ASC, `schedule_date` ASC, `time_slot` ASC) USING BTREE,
  CONSTRAINT `schedule_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 705 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '医生排班表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for schedule_rule
-- ----------------------------
DROP TABLE IF EXISTS `schedule_rule`;
CREATE TABLE `schedule_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '规则唯一ID',
  `rule_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `rule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则类型 (weekly/custom/template)',
  `doctor_id` bigint NULL DEFAULT NULL COMMENT '关联医生ID (可为空表示科室规则)',
  `department_id` bigint NULL DEFAULT NULL COMMENT '关联科室ID',
  `clinic_id` bigint NULL DEFAULT NULL COMMENT '关联门诊ID',
  `week_days` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '星期配置 (如: 1,3,5 表示周一三五)',
  `time_slots` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '时段配置 (如: morning,afternoon)',
  `start_date` date NULL DEFAULT NULL COMMENT '规则生效开始日期',
  `end_date` date NULL DEFAULT NULL COMMENT '规则生效结束日期',
  `slot_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'normal' COMMENT '号别类型 (normal/expert/vip)',
  `total_slots` int NOT NULL COMMENT '每次排班的总号源数',
  `max_daily_schedules` int NULL DEFAULT NULL COMMENT '每天最多排班次数',
  `max_continuous_days` int NULL DEFAULT NULL COMMENT '最多连续出诊天数',
  `skip_weekends` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否跳过周末',
  `skip_holidays` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否跳过节假日',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT '规则状态 (ACTIVE/INACTIVE/EXPIRED)',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级 (数字越大优先级越高)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '规则描述说明',
  `created_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `department_id`(`department_id` ASC) USING BTREE,
  INDEX `clinic_id`(`clinic_id` ASC) USING BTREE,
  INDEX `idx_rule_doctor`(`doctor_id` ASC) USING BTREE,
  INDEX `idx_rule_status`(`status` ASC) USING BTREE,
  CONSTRAINT `schedule_rule_ibfk_1` FOREIGN KEY (`doctor_id`) REFERENCES `doctor` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `schedule_rule_ibfk_2` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `schedule_rule_ibfk_3` FOREIGN KEY (`clinic_id`) REFERENCES `clinic` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '排班规则配置表 - 管理员可设置自动化排班策略' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置唯一ID',
  `key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项键名 (如：STUDENT_REIMBURSEMENT_RATE)',
  `value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置项值',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项描述',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统全局配置与规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录用户名/凭证',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '身份角色 (patient, doctor, admin)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '账户状态 (active, inactive, pending_approval)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 116 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一用户登录与身份信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for waitlist
-- ----------------------------
DROP TABLE IF EXISTS `waitlist`;
CREATE TABLE `waitlist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '候补记录唯一ID',
  `schedule_id` bigint NOT NULL COMMENT '目标排班ID',
  `patient_id` bigint NOT NULL COMMENT '候补的患者ID',
  `join_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入候补队列的时间 (用于确定优先级)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'WAITING' COMMENT '候补状态 (WAITING, NOTIFIED, GRANTED, EXPIRED)',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_waitlist_unique`(`schedule_id` ASC, `patient_id` ASC) USING BTREE,
  INDEX `patient_id`(`patient_id` ASC) USING BTREE,
  CONSTRAINT `waitlist_ibfk_1` FOREIGN KEY (`schedule_id`) REFERENCES `schedule` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `waitlist_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '预约候补队列记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for wechat_subscribe
-- ----------------------------
DROP TABLE IF EXISTS `wechat_subscribe`;
CREATE TABLE `wechat_subscribe`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '授权记录唯一ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `patient_id` bigint NULL DEFAULT NULL COMMENT '患者ID',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '微信OpenID',
  `template_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板名称（便于管理）',
  `authorized` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否已授权',
  `authorized_at` datetime NULL DEFAULT NULL COMMENT '授权时间',
  `expires_at` datetime NULL DEFAULT NULL COMMENT '授权过期时间（订阅消息授权有效期）',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_template`(`user_id` ASC, `template_id` ASC) USING BTREE,
  INDEX `idx_openid`(`openid` ASC) USING BTREE,
  INDEX `idx_patient_id`(`patient_id` ASC) USING BTREE,
  CONSTRAINT `wechat_subscribe_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `wechat_subscribe_ibfk_2` FOREIGN KEY (`patient_id`) REFERENCES `patient` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '微信订阅消息授权表 - 记录用户订阅消息授权状态' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for whitelist
-- ----------------------------
DROP TABLE IF EXISTS `whitelist`;
CREATE TABLE `whitelist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '白名单唯一ID',
  `identity_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学号或工号',
  `role_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色类型 (student/teacher/outsider)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'active' COMMENT '状态 (active/inactive)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_whitelist_identity_number`(`identity_number` ASC) USING BTREE,
  INDEX `idx_whitelist_role_type`(`role_type` ASC) USING BTREE,
  INDEX `idx_whitelist_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '身份认证白名单表（学号/工号）' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

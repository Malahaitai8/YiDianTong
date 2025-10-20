-- 医点通数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS yi_dian_tong_2 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yi_dian_tong_2;

-- 1. 用户表
CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    role ENUM('admin', 'doctor', 'patient') NOT NULL COMMENT '角色',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '用户表';

-- 2. 诊所表
CREATE TABLE clinic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '诊所名称',
    description TEXT COMMENT '诊所描述',
    address VARCHAR(255) COMMENT '诊所地址',
    phone VARCHAR(20) COMMENT '联系电话',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '诊所表';

-- 3. 科室表
CREATE TABLE department (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    clinic_id BIGINT NOT NULL COMMENT '诊所ID',
    name VARCHAR(50) NOT NULL COMMENT '科室名称',
    description TEXT COMMENT '科室描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (clinic_id) REFERENCES clinic(id) ON DELETE CASCADE
) COMMENT '科室表';

-- 4. 医生表
CREATE TABLE doctor (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    clinic_id BIGINT NOT NULL COMMENT '诊所ID',
    name VARCHAR(50) NOT NULL COMMENT '医生姓名',
    title VARCHAR(50) COMMENT '职称',
    specialty VARCHAR(100) COMMENT '专业领域',
    bio TEXT COMMENT '个人简介',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (clinic_id) REFERENCES clinic(id) ON DELETE CASCADE
) COMMENT '医生表';

-- 5. 患者表
CREATE TABLE patient (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '患者姓名',
    specific_role VARCHAR(50) COMMENT '具体角色',
    id_status TINYINT DEFAULT 0 COMMENT '身份验证状态：0-未验证，1-已验证',
    phone_number VARCHAR(20) COMMENT '手机号码',
    id_card_number VARCHAR(18) COMMENT '身份证号',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '患者表';

-- 6. 管理员表
CREATE TABLE admin (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '管理员姓名',
    permissions TEXT COMMENT '权限列表',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) COMMENT '管理员表';

-- 7. 排班表
CREATE TABLE schedule (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    department_id BIGINT NOT NULL COMMENT '科室ID',
    date DATE NOT NULL COMMENT '排班日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    max_appointments INT DEFAULT 20 COMMENT '最大预约数',
    current_appointments INT DEFAULT 0 COMMENT '当前预约数',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-取消',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
) COMMENT '排班表';

-- 8. 预约表
CREATE TABLE appointment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    schedule_id BIGINT NOT NULL COMMENT '排班ID',
    appointment_time DATETIME NOT NULL COMMENT '预约时间',
    status ENUM('pending', 'confirmed', 'completed', 'cancelled') DEFAULT 'pending' COMMENT '预约状态',
    notes TEXT COMMENT '备注',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedule(id) ON DELETE CASCADE
) COMMENT '预约表';

-- 9. 候诊表
CREATE TABLE waitlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL COMMENT '患者ID',
    doctor_id BIGINT NOT NULL COMMENT '医生ID',
    queue_number INT NOT NULL COMMENT '排队号码',
    status ENUM('waiting', 'called', 'completed', 'cancelled') DEFAULT 'waiting' COMMENT '候诊状态',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (patient_id) REFERENCES patient(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id) ON DELETE CASCADE
) COMMENT '候诊表';

-- 10. 系统配置表
CREATE TABLE system_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    config_key VARCHAR(100) NOT NULL UNIQUE COMMENT '配置键',
    config_value TEXT COMMENT '配置值',
    description VARCHAR(255) COMMENT '配置描述',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) COMMENT '系统配置表';

-- 插入初始数据
-- 创建默认诊所
INSERT INTO clinic (name, description, address, phone) VALUES 
('医点通诊所', '提供全面医疗服务的现代化诊所', '北京市朝阳区医疗街123号', '010-12345678');

-- 创建默认科室
INSERT INTO department (clinic_id, name, description) VALUES 
(1, '内科', '内科疾病诊疗'),
(1, '外科', '外科手术及治疗'),
(1, '儿科', '儿童疾病专科'),
(1, '妇科', '妇科疾病诊疗');

-- 创建默认管理员用户
INSERT INTO user (username, password, role, status) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'admin', 1);

-- 创建管理员信息
INSERT INTO admin (user_id, name, permissions) VALUES 
(1, '系统管理员', 'ALL');

-- 创建示例医生用户
INSERT INTO user (username, password, role, status) VALUES 
('doctor1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'doctor', 1),
('doctor2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'doctor', 1);

-- 创建医生信息
INSERT INTO doctor (user_id, clinic_id, name, title, specialty, bio) VALUES 
(2, 1, '张医生', '主任医师', '内科', '从事内科临床工作20年，擅长心血管疾病诊治'),
(3, 1, '李医生', '副主任医师', '外科', '从事外科临床工作15年，擅长微创手术');

-- 创建示例患者用户
INSERT INTO user (username, password, role, status) VALUES 
('patient1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDi', 'patient', 1);

-- 创建患者信息
INSERT INTO patient (user_id, name, specific_role, id_status, phone_number, id_card_number) VALUES 
(4, '王患者', '普通患者', 1, '13800138000', '110101199001011234');

-- 创建系统配置
INSERT INTO system_config (config_key, config_value, description) VALUES 
('system_name', '医点通挂号系统', '系统名称'),
('max_appointments_per_day', '50', '每日最大预约数'),
('appointment_advance_days', '7', '可提前预约天数');

-- 创建示例排班
INSERT INTO schedule (doctor_id, department_id, date, start_time, end_time, max_appointments) VALUES 
(1, 1, CURDATE() + INTERVAL 1 DAY, '09:00:00', '12:00:00', 20),
(1, 1, CURDATE() + INTERVAL 1 DAY, '14:00:00', '17:00:00', 20),
(2, 2, CURDATE() + INTERVAL 1 DAY, '08:00:00', '12:00:00', 15),
(2, 2, CURDATE() + INTERVAL 2 DAY, '14:00:00', '18:00:00', 15);
INSERT INTO `department` (`name`, `description`) VALUES
('内科', '负责内脏器官疾病诊断与非手术治疗，包括常见疾病和慢性病管理。'),
('外科', '负责外伤、肿瘤、感染等需手术治疗的疾病。'),
('妇产科', '负责女性生殖系统疾病及妊娠、分娩、产后健康管理。'),
('儿科', '负责儿童各阶段的健康与疾病诊治。'),
('中医科', '提供传统中医疗法，如针灸、推拿和中药调理。'),
('五官科', '耳鼻喉、眼科及口腔疾病的诊断与治疗。');

INSERT INTO `clinic` (`department_id`, `name`, `description`) VALUES
(1, '呼吸内科门诊', '常见呼吸道感染、哮喘、慢性阻塞性肺疾病等诊治。'),
(1, '心血管内科门诊', '高血压、冠心病、心律失常等诊治。'),
(1, '消化内科门诊', '胃炎、肠炎、消化道出血等诊治。'),
(1, '内分泌科门诊', '糖尿病、甲状腺疾病、肥胖等诊治。'),
(1, '神经内科门诊', '头痛、眩晕、失眠等神经系统常见疾病诊治。'),
(2, '普通外科门诊', '体表肿物、甲状腺、乳腺疾病及腹股沟疝等诊治。'),
(2, '骨科门诊', '骨折、脱位、运动损伤、颈肩腰腿痛等诊治。'),
(2, '泌尿外科门诊', '泌尿系统结石、感染等诊治。'),
(2, '烧伤整形外科门诊', '各类烧伤、创面处理及体表美容咨询。'),
(2, '肛肠外科门诊', '痔疮、肛裂等常见肛肠疾病诊治。'),
(3, '妇科门诊', '月经不调、妇科炎症、宫颈疾病等诊治。'),
(4, '儿科普通门诊', '儿童常见感冒、发烧、腹泻等疾病诊治。'),
(5, '中医内科门诊', '中医体质调理、慢性病的中药治疗。'),
(6, '耳鼻喉科门诊', '鼻炎、咽炎、扁桃体炎等疾病诊治。'),
(6, '眼科门诊', '近视、结膜炎、干眼症等诊治。');

INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('doctor_lwh1', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_zxw2', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_wjj3', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_zm4', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_lq5', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_cl6', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_sdw7', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_zm8', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_wf9', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_zw10', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_ql11', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_ft12', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_gy13', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_wd14', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_xhy15', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_xl16', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_zb17', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_qy18', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_cy19', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_yy20', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_yj21', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_xm22', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_hj23', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_cl24', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_dh25', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_xq26', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_lp27', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_gml28', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_tl29', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_hw30', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_mt31', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_zy32', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_cl33', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_pg34', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_hj35', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_jh36', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active'),
('doctor_xf37', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'doctor', 'active');

INSERT INTO `doctor` (`user_id`, `clinic_id`, `name`, `title`, `specialty`, `bio`) VALUES
(1, 1, '李文华', '主任医师', '慢性咳嗽、哮喘、肺部感染', '资深呼吸内科专家，擅长呼吸系统疑难病诊治。'),
(2, 1, '张晓雯', '副主任医师', '戒烟咨询、肺功能检查解读', '专注于呼吸健康宣教和慢病管理。'),
(3, 1, '王建军', '主治医师', '常见感冒、支气管炎', '内科常见疾病诊疗经验丰富。'),
(4, 1, '赵梅', '主治医师', '呼吸系统慢病管理', '耐心细致，提供个性化慢病指导。'),
(5, 2, '刘强', '主任医师', '高血压、冠心病、心绞痛', '心血管领域权威，侧重高危患者管理。'),
(6, 2, '陈琳', '副主任医师', '心律失常及心电图判读', '精通心律失常的药物治疗。'),
(7, 2, '孙大为', '主治医师', '基础心血管疾病筛查', '擅长早期风险评估与生活方式干预。'),
(8, 3, '周明', '主任医师', '胃肠道常见疾病、幽门螺杆菌感染', '擅长消化系统常见病和多发病。'),
(9, 3, '吴芳', '主治医师', '功能性胃肠病、腹痛腹泻', '专治各种非器质性消化道不适。'),
(10, 4, '郑伟', '副主任医师', '糖尿病诊断与个体化治疗', '对代谢综合征有深入研究。'),
(11, 4, '钱丽', '主治医师', '甲状腺功能异常、内分泌失调', '关注年轻群体内分泌健康。'),
(12, 5, '冯涛', '主任医师', '头痛、偏头痛、睡眠障碍', '神经内科专家，尤其擅长慢性疼痛管理。'),
(13, 5, '顾悦', '主治医师', '眩晕症、焦虑状态', '提供神经心理咨询与治疗。'),
(14, 6, '魏东', '主任医师', '体表肿物、小创伤处理、疝气', '普通外科领域经验丰富，处理各种小手术。'),
(15, 6, '徐海燕', '副主任医师', '甲状腺、乳腺疾病筛查', '侧重女性外科疾病的早期诊断。'),
(16, 6, '许磊', '主治医师', '基础外科疾病、换药及术后随访', '细致耐心，擅长创面护理。'),
(17, 7, '朱斌', '副主任医师', '运动损伤、关节疼痛、颈椎病', '擅长非手术治疗各类骨科疼痛。'),
(18, 7, '秦雨', '主治医师', '软组织损伤、骨折复位后随诊', '专注于急性损伤和康复指导。'),
(19, 8, '曹阳', '主任医师', '泌尿系统感染、前列腺疾病', '泌尿生殖系统常见疾病诊治专家。'),
(20, 8, '袁圆', '主治医师', '尿路结石、血尿初筛', '擅长泌尿系超声检查与结果分析。'),
(21, 9, '杨洁', '副主任医师', '轻度烧烫伤处理、慢性创面修复', '精于皮肤损伤的紧急与后期处理。'),
(22, 9, '薛明', '主治医师', '外伤缝合、疤痕咨询', '提供专业的美容与疤痕预防建议。'),
(23, 10, '韩建', '主治医师', '痔疮、肛裂、肛周脓肿', '擅长常见肛肠疾病的诊疗与保守治疗。'),
(24, 10, '程璐', '主治医师', '便秘、排便异常咨询', '注重饮食与生活方式的干预指导。'),
(25, 11, '丁慧', '主任医师', '妇科常见炎症、月经病', '经验丰富，专注于女性生殖健康。'),
(26, 11, '肖晴', '副主任医师', '妇科内分泌调理、更年期综合征', '擅长激素替代疗法和内分泌平衡。'),
(27, 11, '陆萍', '主治医师', '避孕指导、宫颈疾病初筛', '年轻有活力，提供贴心的健康指导。'),
(28, 12, '郭美玲', '主任医师', '儿童呼吸道、消化道常见病', '对小儿常见病有全面的诊治能力。'),
(29, 12, '谭磊', '副主任医师', '儿童生长发育评估', '专注于儿童健康管理和发育监测。'),
(30, 12, '何薇', '主治医师', '小儿发热、感染性疾病', '善于处理急性感染和发热。'),
(31, 12, '孟涛', '主治医师', '疫苗接种咨询及常见过敏', '提供专业的儿童免疫和过敏咨询。'),
(32, 13, '庄严', '主任中医师', '中医体质辨识、亚健康调理', '资深中医师，注重整体调理。'),
(33, 13, '崔丽', '主治中医师', '中药、针灸、推拿', '擅长将传统疗法与现代医学结合。'),
(34, 14, '彭刚', '副主任医师', '鼻炎、鼻窦炎、咽喉炎', '专注于上呼吸道感染和炎症治疗。'),
(35, 14, '侯静', '主治医师', '突发耳聋、眩晕初诊', '擅长耳部疾病的检查和初筛。'),
(36, 15, '江海', '主任医师', '屈光不正、干眼症、视疲劳', '眼科常见病诊治专家，尤其擅长青少年近视管理。'),
(37, 15, '夏芳', '主治医师', '结膜炎、麦粒肿等眼部感染', '提供眼部日常护理与咨询。');

-- 排班数据
INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(1, '2025-10-14', 'morning', 'expert', 10, 10),
(5, '2025-10-14', 'morning', 'expert', 10, 10),
(8, '2025-10-14', 'morning', 'normal', 20, 20),
(10, '2025-10-14', 'afternoon', 'normal', 20, 20),
(12, '2025-10-14', 'afternoon', 'normal', 20, 20),
(14, '2025-10-14', 'morning', 'expert', 10, 10),
(17, '2025-10-14', 'afternoon', 'normal', 20, 20),
(19, '2025-10-14', 'morning', 'normal', 20, 20),
(21, '2025-10-14', 'afternoon', 'normal', 20, 20),
(23, '2025-10-14', 'morning', 'normal', 20, 20),
(25, '2025-10-14', 'morning', 'expert', 10, 10),
(28, '2025-10-14', 'afternoon', 'expert', 10, 10),
(32, '2025-10-14', 'morning', 'normal', 20, 20),
(34, '2025-10-14', 'afternoon', 'normal', 20, 20),
(36, '2025-10-14', 'morning', 'normal', 20, 20),
(2, '2025-10-14', 'afternoon', 'normal', 20, 20),
(15, '2025-10-14', 'afternoon', 'normal', 20, 20),
(29, '2025-10-14', 'morning', 'normal', 20, 20);

INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(3, '2025-10-15', 'afternoon', 'normal', 20, 20),
(6, '2025-10-15', 'morning', 'normal', 20, 20),
(9, '2025-10-15', 'afternoon', 'normal', 20, 20),
(11, '2025-10-15', 'morning', 'normal', 20, 20),
(13, '2025-10-15', 'morning', 'normal', 20, 20),
(16, '2025-10-15', 'afternoon', 'normal', 20, 20),
(18, '2025-10-15', 'morning', 'normal', 20, 20),
(20, '2025-10-15', 'afternoon', 'normal', 20, 20),
(22, '2025-10-15', 'morning', 'normal', 20, 20),
(24, '2025-10-15', 'afternoon', 'normal', 20, 20),
(26, '2025-10-15', 'afternoon', 'normal', 20, 20),
(30, '2025-10-15', 'morning', 'normal', 20, 20),
(33, '2025-10-15', 'afternoon', 'normal', 20, 20),
(35, '2025-10-15', 'morning', 'normal', 20, 20),
(37, '2025-10-15', 'afternoon', 'normal', 20, 20),
(4, '2025-10-15', 'morning', 'normal', 20, 20),
(7, '2025-10-15', 'afternoon', 'normal', 20, 20),
(27, '2025-10-15', 'morning', 'normal', 20, 20);

INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(1, '2025-10-16', 'morning', 'expert', 10, 10),
(5, '2025-10-16', 'afternoon', 'expert', 10, 10),
(8, '2025-10-16', 'morning', 'normal', 20, 20),
(10, '2025-10-16', 'afternoon', 'normal', 20, 20),
(12, '2025-10-16', 'morning', 'normal', 20, 20),
(14, '2025-10-16', 'afternoon', 'expert', 10, 10),
(17, '2025-10-16', 'morning', 'normal', 20, 20),
(19, '2025-10-16', 'afternoon', 'normal', 20, 20),
(21, '2025-10-16', 'morning', 'normal', 20, 20),
(23, '2025-10-16', 'afternoon', 'normal', 20, 20),
(25, '2025-10-16', 'morning', 'expert', 10, 10),
(28, '2025-10-16', 'afternoon', 'expert', 10, 10),
(32, '2025-10-16', 'morning', 'normal', 20, 20),
(34, '2025-10-16', 'afternoon', 'normal', 20, 20),
(36, '2025-10-16', 'morning', 'normal', 20, 20),
(3, '2025-10-16', 'afternoon', 'normal', 20, 20),
(16, '2025-10-16', 'morning', 'normal', 20, 20),
(31, '2025-10-16', 'morning', 'normal', 20, 20);

INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(2, '2025-10-17', 'afternoon', 'normal', 20, 20),
(6, '2025-10-17', 'morning', 'normal', 20, 20),
(9, '2025-10-17', 'afternoon', 'normal', 20, 20),
(11, '2025-10-17', 'morning', 'normal', 20, 20),
(13, '2025-10-17', 'afternoon', 'normal', 20, 20),
(15, '2025-10-17', 'morning', 'normal', 20, 20),
(18, '2025-10-17', 'afternoon', 'normal', 20, 20),
(20, '2025-10-17', 'morning', 'normal', 20, 20),
(22, '2025-10-17', 'afternoon', 'normal', 20, 20),
(24, '2025-10-17', 'morning', 'normal', 20, 20),
(26, '2025-10-17', 'afternoon', 'normal', 20, 20),
(29, '2025-10-17', 'morning', 'normal', 20, 20),
(33, '2025-10-17', 'afternoon', 'normal', 20, 20),
(35, '2025-10-17', 'morning', 'normal', 20, 20),
(37, '2025-10-17', 'afternoon', 'normal', 20, 20),
(4, '2025-10-17', 'morning', 'normal', 20, 20),
(7, '2025-10-17', 'afternoon', 'normal', 20, 20),
(27, '2025-10-17', 'morning', 'normal', 20, 20);

INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(1, '2025-10-18', 'morning', 'expert', 10, 10),
(5, '2025-10-18', 'morning', 'expert', 10, 10),
(8, '2025-10-18', 'morning', 'normal', 20, 20),
(10, '2025-10-18', 'afternoon', 'normal', 20, 20),
(12, '2025-10-18', 'afternoon', 'normal', 20, 20),
(14, '2025-10-18', 'morning', 'expert', 10, 10),
(17, '2025-10-18', 'afternoon', 'normal', 20, 20),
(19, '2025-10-18', 'morning', 'normal', 20, 20),
(21, '2025-10-18', 'afternoon', 'normal', 20, 20),
(23, '2025-10-18', 'morning', 'normal', 20, 20),
(25, '2025-10-18', 'morning', 'expert', 10, 10),
(28, '2025-10-18', 'afternoon', 'expert', 10, 10),
(32, '2025-10-18', 'morning', 'normal', 20, 20),
(34, '2025-10-18', 'afternoon', 'normal', 20, 20),
(36, '2025-10-18', 'morning', 'normal', 20, 20),
(2, '2025-10-18', 'afternoon', 'normal', 20, 20),
(15, '2025-10-18', 'afternoon', 'normal', 20, 20),
(30, '2025-10-18', 'morning', 'normal', 20, 20);

INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(3, '2025-10-19', 'afternoon', 'normal', 20, 20),
(6, '2025-10-19', 'morning', 'normal', 20, 20),
(9, '2025-10-19', 'afternoon', 'normal', 20, 20),
(11, '2025-10-19', 'morning', 'normal', 20, 20),
(13, '2025-10-19', 'morning', 'normal', 20, 20),
(16, '2025-10-19', 'afternoon', 'normal', 20, 20),
(18, '2025-10-19', 'morning', 'normal', 20, 20),
(20, '2025-10-19', 'afternoon', 'normal', 20, 20),
(22, '2025-10-19', 'morning', 'normal', 20, 20),
(24, '2025-10-19', 'afternoon', 'normal', 20, 20),
(26, '2025-10-19', 'afternoon', 'normal', 20, 20),
(29, '2025-10-19', 'morning', 'normal', 20, 20),
(33, '2025-10-19', 'afternoon', 'normal', 20, 20),
(35, '2025-10-19', 'morning', 'normal', 20, 20),
(37, '2025-10-19', 'afternoon', 'normal', 20, 20),
(4, '2025-10-19', 'morning', 'normal', 20, 20),
(7, '2025-10-19', 'afternoon', 'normal', 20, 20),
(27, '2025-10-19', 'morning', 'normal', 20, 20);

INSERT INTO `schedule` (`doctor_id`, `schedule_date`, `time_slot`, `slot_type`, `total_slots`, `available_slots`) VALUES
(1, '2025-10-20', 'morning', 'expert', 10, 10),
(5, '2025-10-20', 'afternoon', 'expert', 10, 10),
(8, '2025-10-20', 'morning', 'normal', 20, 20),
(10, '2025-10-20', 'afternoon', 'normal', 20, 20),
(12, '2025-10-20', 'morning', 'normal', 20, 20),
(14, '2025-10-20', 'afternoon', 'expert', 10, 10),
(17, '2025-10-20', 'morning', 'normal', 20, 20),
(19, '2025-10-20', 'afternoon', 'normal', 20, 20),
(21, '2025-10-20', 'morning', 'normal', 20, 20),
(23, '2025-10-20', 'afternoon', 'normal', 20, 20),
(25, '2025-10-20', 'morning', 'expert', 10, 10),
(28, '2025-10-20', 'afternoon', 'expert', 10, 10),
(32, '2025-10-20', 'morning', 'normal', 20, 20),
(34, '2025-10-20', 'afternoon', 'normal', 20, 20),
(36, '2025-10-20', 'morning', 'normal', 20, 20),
(2, '2025-10-20', 'afternoon', 'normal', 20, 20),
(16, '2025-10-20', 'morning', 'normal', 20, 20),
(31, '2025-10-20', 'morning', 'normal', 20, 20);
-- ========================================
-- 测试账号数据
-- ========================================
-- 所有测试账号密码均为: 123456
-- BCrypt加密后: $2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu
-- 注意：此哈希值已通过BCrypt验证，对应密码 123456
-- 如果遇到密码不匹配问题，可以使用应用程序的 /test/password/reset-admin 端点重置密码
-- ========================================

-- 1. 管理员测试账号
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('admin', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'admin', 'active');

INSERT INTO `admin` (`user_id`, `name`, `admin_role`) VALUES
(LAST_INSERT_ID(), '系统管理员', '超级管理员');

-- ========================================
-- 2. 白名单测试数据（必须在患者数据之前插入）
-- ========================================
-- 学生学号白名单
INSERT INTO `whitelist` (`identity_number`, `role_type`, `status`) VALUES
('2021001001', 'student', 'active'),
('2021001002', 'student', 'active'),
('2021001003', 'student', 'active'),
('2021001004', 'student', 'active'),
('2021001005', 'student', 'active'),
('2022001001', 'student', 'active'),
('2022001002', 'student', 'active'),
('2023001001', 'student', 'active');

-- 教师工号白名单
INSERT INTO `whitelist` (`identity_number`, `role_type`, `status`) VALUES
('T2021001', 'teacher', 'active'),
('T2021002', 'teacher', 'active'),
('T2021003', 'teacher', 'active'),
('T2022001', 'teacher', 'active'),
('T2022002', 'teacher', 'active');

-- ========================================
-- 3. 患者测试账号
-- ========================================
-- 3.1 已认证的学生账号（用于测试已认证用户的功能）
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('student1', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @student1_id = LAST_INSERT_ID();

INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('student2', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @student2_id = LAST_INSERT_ID();

-- 插入已认证的学生患者信息（已通过身份认证）
INSERT INTO `patient` (`user_id`, `name`, `specific_role`, `id_status`, `phone_number`, `identity_number`, `id_card_number`) VALUES
(@student1_id, '张小明', 'student', 'verified', '13800138001', '2021001001', '110101199001011234'),
(@student2_id, '李小红', 'student', 'verified', '13800138002', '2021001002', '110101199002021234');

-- 3.2 未认证的学生账号（用于测试注册和认证流程）
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('student3', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @student3_id = LAST_INSERT_ID();

INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('student4', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @student4_id = LAST_INSERT_ID();

-- 插入未认证的学生患者信息（注册时只填写手机号，其他字段为NULL）
INSERT INTO `patient` (`user_id`, `name`, `specific_role`, `id_status`, `phone_number`, `identity_number`, `id_card_number`) VALUES
(@student3_id, NULL, NULL, 'pending', '13800138003', NULL, NULL),
(@student4_id, NULL, NULL, 'pending', '13800138004', NULL, NULL);

-- 3.3 已认证的教师账号
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('teacher1', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @teacher1_id = LAST_INSERT_ID();

INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('teacher2', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @teacher2_id = LAST_INSERT_ID();

-- 插入已认证的教师患者信息（已通过身份认证）
INSERT INTO `patient` (`user_id`, `name`, `specific_role`, `id_status`, `phone_number`, `identity_number`, `id_card_number`) VALUES
(@teacher1_id, '赵老师', 'teacher', 'verified', '13900139001', 'T2021001', '110101198001011234'),
(@teacher2_id, '钱老师', 'teacher', 'verified', '13900139002', 'T2021002', '110101198002021234');

-- 3.4 未认证的教师账号（用于测试注册和认证流程）
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('teacher3', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @teacher3_id = LAST_INSERT_ID();

-- 插入未认证的教师患者信息（注册时只填写手机号，其他字段为NULL）
INSERT INTO `patient` (`user_id`, `name`, `specific_role`, `id_status`, `phone_number`, `identity_number`, `id_card_number`) VALUES
(@teacher3_id, NULL, NULL, 'pending', '13900139003', NULL, NULL);

-- 3.5 校外人员账号（学号/工号不在白名单中）
INSERT INTO `user` (`username`, `password`, `role`, `status`) VALUES
('outsider1', '$2a$10$6qVVSBhnSGhY6A/0iyFqqukp0aJaSqO0zUgt75jqTEriamesQwjBu', 'patient', 'active');
SET @outsider1_id = LAST_INSERT_ID();

-- 插入已认证的校外人员患者信息（认证时学号不在白名单，自动设置为outsider）
INSERT INTO `patient` (`user_id`, `name`, `specific_role`, `id_status`, `phone_number`, `identity_number`, `id_card_number`) VALUES
(@outsider1_id, '校外人员', 'outsider', 'verified', '13700137001', 'OUTSIDER001', '110101199501011234');

-- ========================================
-- 测试账号清单
-- ========================================
-- 管理员: admin / 123456
-- 
-- 已认证学生账号:
--   student1 / 123456 (张小明 - 学号: 2021001001 - 已认证)
--   student2 / 123456 (李小红 - 学号: 2021001002 - 已认证)
-- 
-- 未认证学生账号（用于测试认证流程）:
--   student3 / 123456 (手机号: 13800138003 - 待认证，学号2021001003在白名单中)
--   student4 / 123456 (手机号: 13800138004 - 待认证，学号2021001004在白名单中)
-- 
-- 已认证教师账号:
--   teacher1 / 123456 (赵老师 - 工号: T2021001 - 已认证)
--   teacher2 / 123456 (钱老师 - 工号: T2021002 - 已认证)
-- 
-- 未认证教师账号（用于测试认证流程）:
--   teacher3 / 123456 (手机号: 13900139003 - 待认证，工号T2021003在白名单中)
-- 
-- 校外人员账号:
--   outsider1 / 123456 (校外人员 - 学号: OUTSIDER001 - 已认证，不在白名单中)
-- 
-- 白名单数据:
--   学生学号: 2021001001-2021001005, 2022001001-2022001002, 2023001001
--   教师工号: T2021001-T2021003, T2022001-T2022002
-- ========================================

-- ========================================
-- 排班规则示例数据
-- ========================================
-- 示例1: 李文华医生(呼吸内科)固定周一三五上午出诊
INSERT INTO `schedule_rule` (
    `rule_name`, `rule_type`, `doctor_id`, `clinic_id`,
    `week_days`, `time_slots`, `start_date`, `end_date`,
    `slot_type`, `total_slots`, `skip_weekends`, `skip_holidays`,
    `status`, `priority`, `description`, `created_by`
) VALUES (
    '李文华专家门诊规则',
    'weekly',
    1,
    1,
    '1,3,5',
    'morning',
    '2025-11-01',
    '2025-12-31',
    'expert',
    10,
    1,
    1,
    'ACTIVE',
    10,
    '呼吸内科专家李文华医生每周一三五上午出诊，专家号10个',
    'admin'
);

-- 示例2: 刘强医生(心血管内科)周二四下午出诊
INSERT INTO `schedule_rule` (
    `rule_name`, `rule_type`, `doctor_id`, `clinic_id`,
    `week_days`, `time_slots`, `start_date`, `end_date`,
    `slot_type`, `total_slots`, `max_continuous_days`, `skip_weekends`,
    `status`, `priority`, `description`, `created_by`
) VALUES (
    '刘强心血管专家规则',
    'weekly',
    5,
    2,
    '2,4',
    'afternoon',
    '2025-11-01',
    '2025-12-31',
    'expert',
    10,
    3,
    1,
    'ACTIVE',
    10,
    '心血管专家刘强医生周二四下午出诊',
    'admin'
);

-- 示例3: 周明医生(消化内科)普通门诊规则
INSERT INTO `schedule_rule` (
    `rule_name`, `rule_type`, `doctor_id`, `clinic_id`,
    `week_days`, `time_slots`, `start_date`, `end_date`,
    `slot_type`, `total_slots`, `skip_weekends`,
    `status`, `priority`, `description`, `created_by`
) VALUES (
    '周明消化科普通门诊',
    'weekly',
    8,
    3,
    '1,2,3,4,5',
    'morning',
    '2025-11-01',
    '2025-12-31',
    'normal',
    20,
    1,
    'ACTIVE',
    5,
    '周明医生工作日上午出诊，普通号20个',
    'admin'
);

-- ========================================
-- 申请记录示例数据
-- ========================================
-- 示例1: 调班申请 - 李文华医生申请取消11月15日上午门诊
INSERT INTO `application_request` (
    `request_type`, `applicant_id`, `applicant_role`,
    `schedule_id`, `change_type`, `original_date`, `original_time_slot`,
    `status`, `reason`, `created_at`
) VALUES (
    'SCHEDULE_CHANGE',
    1,  -- 李文华医生的user_id
    'doctor',
    1,  -- 对应的schedule_id
    'CANCEL',
    '2025-10-14',
    'morning',
    'PENDING',
    '需要参加医学学术会议，无法出诊',
    '2025-10-01 09:00:00'
);

-- 示例2: 调班申请 - 刘强医生申请调整号源数量
INSERT INTO `application_request` (
    `request_type`, `applicant_id`, `applicant_role`,
    `schedule_id`, `change_type`, `original_date`, `original_time_slot`,
    `slot_adjustment`, `status`, `reason`, `created_at`
) VALUES (
    'SCHEDULE_CHANGE',
    5,  -- 刘强医生的user_id
    'doctor',
    2,
    'ADJUST_SLOTS',
    '2025-10-14',
    'morning',
    5,  -- 增加5个号源
    'APPROVED',
    '患者需求量大，希望增加号源',
    '2025-10-02 10:30:00'
);

-- 示例3: 调班申请 - 改期申请
INSERT INTO `application_request` (
    `request_type`, `applicant_id`, `applicant_role`,
    `schedule_id`, `change_type`,
    `original_date`, `original_time_slot`,
    `new_date`, `new_time_slot`,
    `status`, `reason`, `reject_reason`, `reviewer_id`, `reviewed_at`, `created_at`
) VALUES (
    'SCHEDULE_CHANGE',
    8,  -- 周明医生的user_id
    'doctor',
    3,
    'RESCHEDULE',
    '2025-10-14',
    'morning',
    '2025-10-15',
    'afternoon',
    'REJECTED',
    '个人原因需要调整出诊时间',
    '10月15日下午已有其他医生排班，无法安排',
    38,  -- admin的user_id
    '2025-10-03 14:20:00',
    '2025-10-03 11:00:00'
);

-- 示例4: 信息修改申请 - 张晓雯医生申请修改个人简介
INSERT INTO `application_request` (
    `request_type`, `applicant_id`, `applicant_role`,
    `doctor_id`, `field_name`, `old_value`, `new_value`,
    `status`, `reason`, `created_at`
) VALUES (
    'INFO_UPDATE',
    2,  -- 张晓雯医生的user_id
    'doctor',
    2,  -- 张晓雯医生的doctor_id
    'bio',
    '专注于呼吸健康宣教和慢病管理。',
    '专注于呼吸健康宣教和慢病管理，擅长戒烟咨询和肺功能康复指导。',
    'PENDING',
    '补充完善个人专业特长介绍',
    '2025-10-04 09:15:00'
);

-- 示例5: 信息修改申请 - 王建军医生申请修改擅长领域
INSERT INTO `application_request` (
    `request_type`, `applicant_id`, `applicant_role`,
    `doctor_id`, `field_name`, `old_value`, `new_value`,
    `status`, `reason`, `reviewer_id`, `reviewed_at`, `created_at`
) VALUES (
    'INFO_UPDATE',
    3,  -- 王建军医生的user_id
    'doctor',
    3,  -- 王建军医生的doctor_id
    'specialty',
    '常见感冒、支气管炎',
    '常见感冒、支气管炎、过敏性鼻炎',
    'APPROVED',
    '新增过敏性疾病诊疗能力',
    38,  -- admin的user_id
    '2025-10-05 16:45:00',
    '2025-10-05 10:20:00'
);

-- 示例6: 信息修改申请 - 赵梅医生申请修改职称
INSERT INTO `application_request` (
    `request_type`, `applicant_id`, `applicant_role`,
    `doctor_id`, `field_name`, `old_value`, `new_value`,
    `status`, `reason`, `created_at`
) VALUES (
    'INFO_UPDATE',
    4,  -- 赵梅医生的user_id
    'doctor',
    4,  -- 赵梅医生的doctor_id
    'title',
    '主治医师',
    '副主任医师',
    'PENDING',
    '已通过职称晋升考试，需要更新系统信息',
    '2025-10-06 14:00:00'
);

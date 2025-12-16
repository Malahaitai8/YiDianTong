package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Whitelist;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.PatientMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private static final Logger logger = LoggerFactory.getLogger(PatientService.class);

    @Resource
    private PatientMapper patientMapper;
    
    @Resource
    private WhitelistService whitelistService;

    public List<Patient> selectAll() {
        List<Patient> list = patientMapper.selectAll();
        return list;
    }

    /**
     * 根据ID查询患者信息
     * 权限检查：管理员可以查询任何患者，患者只能查询自己的信息
     */
    public Patient selectById(Long id) {
        Patient patient = patientMapper.selectById(id);
        if (patient == null) {
            throw new CustomerException("患者不存在");
        }

        // 如果当前用户不是管理员，则检查是否为本人操作
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new CustomerException("401", "未登录");
            }
            
            // 检查是否为本人
            if (!currentUserId.equals(patient.getUserId())) {
                logger.warn("用户 {} 尝试查询患者 {} 的信息，但该患者关联的用户ID为 {}", 
                           currentUserId, id, patient.getUserId());
                throw new CustomerException("403", "无权限查询其他患者的信息");
            }
        }

        return patient;
    }

    public int create(Patient patient) {
        // 校验手机号唯一性
        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().trim().isEmpty()) {
            // 校验手机号格式
            if (!patient.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
                throw new CustomerException("手机号格式不正确，请输入11位有效手机号");
            }
            
            // 检查手机号是否已被使用
            Patient existingPatient = patientMapper.selectByPhoneNumber(patient.getPhoneNumber());
            if (existingPatient != null) {
                throw new CustomerException("该手机号已被注册，请使用其他手机号");
            }
        }
        return patientMapper.insert(patient);
    }

    /**
     * 更新患者信息
     * 权限检查：管理员可以更新任何患者，患者只能更新自己的信息
     */
    public int update(Patient patient) {
        // 检查患者是否存在
        Patient existingPatient = patientMapper.selectById(patient.getId());
        if (existingPatient == null) {
            throw new CustomerException("患者不存在");
        }

        // 如果当前用户不是管理员，则检查是否为本人操作
        if (!SecurityUtils.isAdmin()) {
            Long currentUserId = SecurityUtils.getCurrentUserId();
            if (currentUserId == null) {
                throw new CustomerException("401", "未登录");
            }
            
            // 检查是否为本人
            if (!currentUserId.equals(existingPatient.getUserId())) {
                logger.warn("用户 {} 尝试修改患者 {} 的信息，但该患者关联的用户ID为 {}", 
                           currentUserId, patient.getId(), existingPatient.getUserId());
                throw new CustomerException("403", "无权限修改其他患者的信息");
            }
        }
        
        // 如果更新了手机号，校验唯一性
        if (patient.getPhoneNumber() != null && !patient.getPhoneNumber().trim().isEmpty()) {
            // 校验手机号格式
            if (!patient.getPhoneNumber().matches("^1[3-9]\\d{9}$")) {
                throw new CustomerException("手机号格式不正确，请输入11位有效手机号");
            }
            
            // 检查手机号是否已被其他用户使用
            Patient phonePatient = patientMapper.selectByPhoneNumber(patient.getPhoneNumber());
            if (phonePatient != null && !phonePatient.getId().equals(patient.getId())) {
                throw new CustomerException("该手机号已被其他用户使用，请使用其他手机号");
            }
        }
        
        // 如果更新了身份证号，校验唯一性
        if (patient.getIdCardNumber() != null && !patient.getIdCardNumber().trim().isEmpty()) {
            String newIdCardNumber = patient.getIdCardNumber().trim();
            String oldIdCardNumber = existingPatient.getIdCardNumber();
            
            logger.info("=== 身份证号验证开始 ===");
            logger.info("患者ID: {}", patient.getId());
            logger.info("旧身份证号: [{}]", oldIdCardNumber);
            logger.info("新身份证号: [{}]", newIdCardNumber);
            
            // 判断是否需要检查唯一性
            boolean needCheck = false;
            if (oldIdCardNumber == null || oldIdCardNumber.trim().isEmpty()) {
                needCheck = true;
                logger.info("原身份证号为空，需要检查唯一性");
            } else if (!newIdCardNumber.equals(oldIdCardNumber.trim())) {
                needCheck = true;
                logger.info("身份证号发生变化，需要检查唯一性");
            } else {
                logger.info("身份证号未变化，跳过唯一性检查");
            }
            
            if (needCheck) {
                logger.info("开始查询身份证号 {} 是否已被使用...", newIdCardNumber);
                Patient idCardPatient = patientMapper.selectByIdCardNumber(newIdCardNumber);
                
                if (idCardPatient != null) {
                    logger.error("发现重复！身份证号 {} 已被患者 {} 使用", newIdCardNumber, idCardPatient.getId());
                    throw new CustomerException("该身份证号已被其他用户使用");
                } else {
                    logger.info("身份证号 {} 未被使用，验证通过", newIdCardNumber);
                }
            }
            logger.info("=== 身份证号验证结束 ===");
        }
        
        // 如果更新了学号/工号，自动从白名单中查找并设置角色
        if (patient.getIdentityNumber() != null && !patient.getIdentityNumber().trim().isEmpty()) {
            String newIdentityNumber = patient.getIdentityNumber().trim();
            String oldIdentityNumber = existingPatient.getIdentityNumber();
            
            logger.info("=== 学号/工号验证开始 ===");
            logger.info("患者ID: {}", patient.getId());
            logger.info("旧学号/工号: [{}]", oldIdentityNumber);
            logger.info("新学号/工号: [{}]", newIdentityNumber);
            
            // 判断是否需要检查唯一性
            boolean needCheck = false;
            if (oldIdentityNumber == null || oldIdentityNumber.trim().isEmpty()) {
                needCheck = true;
                logger.info("原学号/工号为空，需要检查唯一性");
            } else if (!newIdentityNumber.equals(oldIdentityNumber.trim())) {
                needCheck = true;
                logger.info("学号/工号发生变化，需要检查唯一性");
            } else {
                logger.info("学号/工号未变化，跳过唯一性检查");
            }
            
            if (needCheck) {
                logger.info("开始查询学号/工号 {} 是否已被使用...", newIdentityNumber);
                Patient identityPatient = patientMapper.selectByIdentityNumber(newIdentityNumber);
                
                if (identityPatient != null) {
                    logger.error("发现重复！学号/工号 {} 已被患者 {} 使用", newIdentityNumber, identityPatient.getId());
                    throw new CustomerException("该学号/工号已被其他用户使用");
                } else {
                    logger.info("学号/工号 {} 未被使用，验证通过", newIdentityNumber);
                }
            }
            
            // 从白名单中查找角色（无论是否变化都要查找，因为可能是首次填写）
            logger.info("开始从白名单查询学号/工号 {}...", newIdentityNumber);
            Whitelist whitelist = whitelistService.selectByIdentityNumber(newIdentityNumber);
            
            if (whitelist != null) {
                logger.info("在白名单中找到！角色类型: {}, 状态: {}", whitelist.getRoleType(), whitelist.getStatus());
                patient.setSpecificRole(whitelist.getRoleType());
                logger.info("已自动设置患者 {} 的角色为: {}", patient.getId(), whitelist.getRoleType());
            } else {
                logger.info("学号/工号 {} 不在白名单中，设置为外来人员", newIdentityNumber);
                patient.setSpecificRole("outsider");
            }
            logger.info("=== 学号/工号验证结束 ===");
        }
        
        // 打印更新前的 patient 对象，确认 specificRole 是否已设置
        logger.info("准备更新患者 {} 到数据库", patient.getId());
        logger.info("  - name: [{}]", patient.getName());
        logger.info("  - identityNumber: [{}]", patient.getIdentityNumber());
        logger.info("  - idCardNumber: [{}]", patient.getIdCardNumber());
        logger.info("  - specificRole: [{}]", patient.getSpecificRole());
        logger.info("  - idStatus: [{}]", patient.getIdStatus());

        int result = patientMapper.update(patient);
        logger.info("患者 {} 更新完成，影响行数: {}", patient.getId(), result);
        
        // 更新后立即查询验证
        Patient updated = patientMapper.selectById(patient.getId());
        logger.info("更新后查询验证 - specificRole: [{}]", updated.getSpecificRole());
        
        return result;
    }

    public int delete(Long id) {
        return patientMapper.delete(id);
    }
    
    /**
     * 获取当前登录患者的个人信息
     * 患者端专用：自动获取当前登录用户关联的患者信息
     */
    public Patient getCurrentPatientProfile() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomerException("401", "未登录");
        }
        
        Patient patient = patientMapper.selectByUserId(currentUserId);
        if (patient == null) {
            throw new CustomerException("患者信息不存在，请先注册");
        }
        
        return patient;
    }
    
    /**
     * 更新当前登录患者的个人信息
     * 患者端专用：只能更新允许的字段（如手机号），不能修改认证相关字段
     * @param phoneNumber 手机号码（可选）
     */
    public void updateCurrentPatientProfile(String phoneNumber) {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomerException("401", "未登录");
        }
        
        // 获取当前患者信息
        Patient patient = patientMapper.selectByUserId(currentUserId);
        if (patient == null) {
            throw new CustomerException("患者信息不存在，请先注册");
        }
        
        // 只更新允许的字段
        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            // 校验手机号格式
            if (!phoneNumber.matches("^1[3-9]\\d{9}$")) {
                throw new CustomerException("手机号格式不正确，请输入11位有效手机号");
            }
            
            // 检查手机号是否已被其他用户使用
            Patient existingPatient = patientMapper.selectByPhoneNumber(phoneNumber);
            if (existingPatient != null && !existingPatient.getId().equals(patient.getId())) {
                throw new CustomerException("该手机号已被其他用户使用，请使用其他手机号");
            }
            
            patient.setPhoneNumber(phoneNumber);
        }
        
        // 更新患者信息
        patientMapper.update(patient);
        logger.info("用户 {} 更新了个人信息", currentUserId);
    }
}


package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.dto.VerifyIdentityRequest;
import com.example.springboot.entity.Patient;
import com.example.springboot.entity.Whitelist;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.PatientMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 身份认证服务类
 */
@Service
public class IdentityVerificationService {

    private static final Logger logger = LoggerFactory.getLogger(IdentityVerificationService.class);

    @Resource
    private PatientMapper patientMapper;

    @Resource
    private WhitelistService whitelistService;

    /**
     * 身份认证
     * 根据学号/工号在白名单中查找，确定用户角色（学生/教师/校外人员）
     * 更新患者信息：姓名、学号/工号、身份证、角色、认证状态
     */
    @Transactional
    public Patient verifyIdentity(VerifyIdentityRequest request) {
        // 1. 获取当前登录用户
        Long currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new CustomerException("401", "未登录");
        }

        // 2. 查询当前用户的患者信息
        Patient patient = patientMapper.selectByUserId(currentUserId);
        if (patient == null) {
            throw new CustomerException("患者信息不存在，请先注册");
        }

        // 3. 检查是否已经认证
        if ("verified".equals(patient.getIdStatus())) {
            throw new CustomerException("您已经完成身份认证，无需重复认证");
        }

        // 4. 检查学号/工号是否已被其他用户使用
        Patient existingPatient = patientMapper.selectByIdentityNumber(request.getIdentityNumber());
        if (existingPatient != null && !existingPatient.getId().equals(patient.getId())) {
            throw new CustomerException("该学号/工号已被其他用户使用");
        }

        // 5. 查询白名单，确定用户角色
        Whitelist whitelist = whitelistService.selectByIdentityNumber(request.getIdentityNumber());
        
        String roleType;
        if (whitelist != null) {
            // 在白名单中，根据白名单的角色类型确定
            roleType = whitelist.getRoleType();
            logger.info("用户 {} 的学号/工号 {} 在白名单中，角色类型: {}", 
                       currentUserId, request.getIdentityNumber(), roleType);
        } else {
            // 不在白名单中，默认为校外人员
            roleType = "outsider";
            logger.info("用户 {} 的学号/工号 {} 不在白名单中，设置为校外人员", 
                       currentUserId, request.getIdentityNumber());
        }

        // 6. 更新患者信息
        patient.setName(request.getName());
        patient.setIdentityNumber(request.getIdentityNumber());
        patient.setIdCardNumber(request.getIdCardNumber());
        patient.setSpecificRole(roleType);
        patient.setIdStatus("verified");

        patientMapper.update(patient);

        logger.info("用户 {} 身份认证成功，角色: {}", currentUserId, roleType);
        return patient;
    }

    /**
     * 根据userId查询患者信息（用于认证接口）
     */
    public Patient getPatientByUserId(Long userId) {
        return patientMapper.selectByUserId(userId);
    }
}


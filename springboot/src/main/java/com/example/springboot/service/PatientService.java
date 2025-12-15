package com.example.springboot.service;

import com.example.springboot.config.SecurityUtils;
import com.example.springboot.entity.Patient;
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

        return patientMapper.update(patient);
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


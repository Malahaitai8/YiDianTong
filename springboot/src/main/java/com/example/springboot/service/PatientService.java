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

        return patientMapper.update(patient);
    }

    public int delete(Long id) {
        return patientMapper.delete(id);
    }
}


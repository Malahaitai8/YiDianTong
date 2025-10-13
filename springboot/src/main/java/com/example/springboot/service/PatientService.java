package com.example.springboot.service;

import com.example.springboot.entity.Patient;
import com.example.springboot.exception.CustomerException;
import com.example.springboot.mapper.PatientMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    @Resource
    private PatientMapper patientMapper;

    public List<Patient> selectAll() {
        List<Patient> list = patientMapper.selectAll();
        return list;
    }

    public Patient selectById(Long id) {
        Patient patient = patientMapper.selectById(id);
        return patient;
    }

    public Patient login(String phoneNumber, String password) {
        Patient patient = patientMapper.selectByPhoneNumber(phoneNumber);
        if (patient == null) {
            throw new CustomerException("该手机号未注册");
        }
        if (!password.equals(patient.getPassword())) {
            throw new CustomerException("密码错误");
        }
        return patient;
    }

    public void register(String name, String phoneNumber, String password, String idCardNumber, String role) {
        // 检查手机号是否已注册
        Patient existingPatient = patientMapper.selectByPhoneNumber(phoneNumber);
        if (existingPatient != null) {
            throw new CustomerException("该手机号已被注册");
        }
        
        // 验证角色
        if (!role.equals("STUDENT") && !role.equals("TEACHER")) {
            throw new CustomerException("角色只能是 STUDENT 或 TEACHER");
        }
        
        // 创建新的患者对象
        Patient patient = new Patient();
        patient.setName(name);
        patient.setPhoneNumber(phoneNumber);
        patient.setPassword(password);
        patient.setIdCardNumber(idCardNumber);
        
        // 设置角色和身份验证状态
        patient.setRole(role);
        patient.setIdStatus("UNVERIFIED");
        
        // 插入新用户
        int result = patientMapper.insert(patient);
        if (result <= 0) {
            throw new CustomerException("注册失败");
        }
    }
}


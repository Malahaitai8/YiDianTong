package com.example.springboot.mapper;

import com.example.springboot.entity.Patient;

import java.util.List;

public interface PatientMapper {
    List<Patient> selectAll();

    Patient selectById(Long id);
    
    // 根据手机号查询病人
    Patient selectByPhoneNumber(String phoneNumber);
    
    // 添加新病人
    int insert(Patient patient);

    int update(Patient patient);

    int delete(Long id);
}


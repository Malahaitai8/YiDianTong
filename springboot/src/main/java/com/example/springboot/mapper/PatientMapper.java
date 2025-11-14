package com.example.springboot.mapper;

import com.example.springboot.entity.Patient;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PatientMapper {
    List<Patient> selectAll();

    Patient selectById(Long id);
    
    // 根据 userId 查询患者
    Patient selectByUserId(Long userId);
    
    // 根据手机号查询病人
    Patient selectByPhoneNumber(String phoneNumber);
    
    // 根据学号/工号查询患者
    Patient selectByIdentityNumber(String identityNumber);
    
    // 添加新病人
    int insert(Patient patient);

    int update(Patient patient);

    int delete(Long id);
}


package com.example.springboot.mapper;

import com.example.springboot.entity.Doctor;

import java.util.List;

public interface DoctorMapper {
    List<Doctor> selectAll();

    Doctor selectById(Long id);

    Doctor selectByUserId(Long userId);

    int insert(Doctor doctor);

    int update(Doctor doctor);

    int delete(Long id);
}


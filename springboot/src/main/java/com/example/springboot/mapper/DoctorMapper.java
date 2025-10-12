package com.example.springboot.mapper;

import com.example.springboot.entity.Doctor;

import java.util.List;

public interface DoctorMapper {
    List<Doctor> selectAll();

    Doctor selectById(Long id);
}


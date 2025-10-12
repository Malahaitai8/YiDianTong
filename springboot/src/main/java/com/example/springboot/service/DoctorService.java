package com.example.springboot.service;

import com.example.springboot.entity.Doctor;
import com.example.springboot.mapper.DoctorMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorService {

    @Resource
    private DoctorMapper doctorMapper;

    public List<Doctor> selectAll() {

        List<Doctor> list = doctorMapper.selectAll();

        return list;
    }

    public Doctor selectById(Long id) {
        Doctor doctor = doctorMapper.selectById(id);
        return doctor;
    }
}


package com.example.springboot.service;

import com.example.springboot.entity.Clinic;
import com.example.springboot.mapper.ClinicMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClinicService {

    @Resource
    private ClinicMapper clinicMapper;

    public List<Clinic> selectAll() {

        List<Clinic> list = clinicMapper.selectAll();

        return list;
    }

    public Clinic selectById(Long id) {
        Clinic clinic = clinicMapper.selectById(id);
        return clinic;
    }
}


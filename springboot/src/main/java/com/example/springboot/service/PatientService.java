package com.example.springboot.service;

import com.example.springboot.entity.Patient;
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
}


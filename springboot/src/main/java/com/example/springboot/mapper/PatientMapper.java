package com.example.springboot.mapper;

import com.example.springboot.entity.Patient;

import java.util.List;

public interface PatientMapper {
    List<Patient> selectAll();

    Patient selectById(Long id);
}


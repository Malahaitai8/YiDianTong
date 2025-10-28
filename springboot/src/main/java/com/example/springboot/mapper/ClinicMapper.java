package com.example.springboot.mapper;

import com.example.springboot.entity.Clinic;

import java.util.List;

public interface ClinicMapper {
    List<Clinic> selectAll();

    Clinic selectById(Long id);

    Clinic selectByName(String name);

    int insert(Clinic clinic);

    int update(Clinic clinic);

    int delete(Long id);
}


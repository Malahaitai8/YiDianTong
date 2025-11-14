package com.example.springboot.mapper;

import com.example.springboot.entity.Clinic;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClinicMapper {
    List<Clinic> selectAll();

    Clinic selectById(Long id);

    Clinic selectByName(String name);

    int insert(Clinic clinic);

    int batchInsert(List<Clinic> clinics);

    int update(Clinic clinic);

    int delete(Long id);
}


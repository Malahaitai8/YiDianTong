package com.example.springboot.mapper;

import com.example.springboot.entity.Clinic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ClinicMapper {
    List<Clinic> selectAll();

    Clinic selectById(Long id);

    Clinic selectByName(String name);
    
    List<Clinic> selectByDepartmentId(@Param("departmentId") Long departmentId);

    int insert(Clinic clinic);

    int batchInsert(List<Clinic> clinics);

    int update(Clinic clinic);

    int delete(Long id);
}


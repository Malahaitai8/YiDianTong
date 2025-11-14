package com.example.springboot.mapper;

import com.example.springboot.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorMapper {
    List<Doctor> selectAll();

    Doctor selectById(Long id);

    Doctor selectByUserId(Long userId);

    List<Doctor> selectByDepartmentId(Long departmentId);

    int insert(Doctor doctor);

    int update(Doctor doctor);

    int delete(Long id);
}


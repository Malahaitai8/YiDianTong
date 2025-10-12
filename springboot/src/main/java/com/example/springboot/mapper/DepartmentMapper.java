package com.example.springboot.mapper;

import com.example.springboot.entity.Department;

import java.util.List;

public interface DepartmentMapper {
    List<Department> selectAll();

    Department selectById(Long id);
}


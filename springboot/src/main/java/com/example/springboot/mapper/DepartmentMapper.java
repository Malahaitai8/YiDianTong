package com.example.springboot.mapper;

import com.example.springboot.entity.Department;

import java.util.List;

public interface DepartmentMapper {
    List<Department> selectAll();

    Department selectById(Long id);

    int insert(Department department);

    int batchInsert(List<Department> departments);

    int update(Department department);

    int delete(Long id);
}


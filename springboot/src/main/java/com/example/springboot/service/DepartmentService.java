package com.example.springboot.service;

import com.example.springboot.entity.Department;
import com.example.springboot.mapper.DepartmentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentService {

    @Resource
    private DepartmentMapper departmentMapper;

    public List<Department> selectAll() {

        List<Department> list = departmentMapper.selectAll();

        return list;
    }

    public Department selectById(Long id) {
        Department department = departmentMapper.selectById(id);
        return department;
    }
}


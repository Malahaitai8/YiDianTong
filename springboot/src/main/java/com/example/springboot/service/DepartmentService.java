package com.example.springboot.service;

import com.example.springboot.entity.Department;
import com.example.springboot.mapper.DepartmentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public int create(Department department) {
        return departmentMapper.insert(department);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchCreate(List<Department> departments) {
        if (departments == null || departments.isEmpty()) {
            throw new IllegalArgumentException("科室列表不能为空");
        }
        return departmentMapper.batchInsert(departments);
    }

    public int update(Department department) {
        return departmentMapper.update(department);
    }

    public int delete(Long id) {
        return departmentMapper.delete(id);
    }
}


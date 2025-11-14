package com.example.springboot.mapper;

import com.example.springboot.entity.Department;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DepartmentMapper {
    List<Department> selectAll();

    Department selectById(Long id);

    int insert(Department department);

    int batchInsert(List<Department> departments);

    int update(Department department);

    int delete(Long id);
}


package com.example.springboot.mapper;

import com.example.springboot.entity.Admin;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AdminMapper {
    List<Admin> selectAll();

    Admin selectById(Long id);

    Admin selectByUserId(Long userId);

    int insert(Admin admin);

    int update(Admin admin);
}


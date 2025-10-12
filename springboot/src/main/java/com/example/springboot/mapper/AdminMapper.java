package com.example.springboot.mapper;

import com.example.springboot.entity.Admin;

import java.util.List;

public interface AdminMapper {
    List<Admin> selectAll();

    Admin selectById(Long id);
}


package com.example.springboot.service;

import com.example.springboot.entity.Admin;
import com.example.springboot.mapper.AdminMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Resource
    private AdminMapper adminMapper;

    public List<Admin> selectAll() {

        List<Admin> list = adminMapper.selectAll();

        return list;
    }

    public Admin selectById(Long id) {
        Admin admin = adminMapper.selectById(id);
        return admin;
    }
}


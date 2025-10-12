package com.example.springboot.mapper;

import com.example.springboot.entity.SystemConfig;

import java.util.List;

public interface SystemConfigMapper {
    List<SystemConfig> selectAll();

    SystemConfig selectById(Long id);
    
    SystemConfig selectByKey(String key);
}


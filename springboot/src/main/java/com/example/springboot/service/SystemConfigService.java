package com.example.springboot.service;

import com.example.springboot.entity.SystemConfig;
import com.example.springboot.mapper.SystemConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SystemConfigService {

    @Resource
    private SystemConfigMapper systemConfigMapper;

    public List<SystemConfig> selectAll() {

        List<SystemConfig> list = systemConfigMapper.selectAll();

        return list;
    }

    public SystemConfig selectById(Long id) {
        SystemConfig systemConfig = systemConfigMapper.selectById(id);
        return systemConfig;
    }
    
    public SystemConfig selectByKey(String key) {
        SystemConfig systemConfig = systemConfigMapper.selectByKey(key);
        return systemConfig;
    }
}


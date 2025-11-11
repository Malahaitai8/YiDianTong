package com.example.springboot.service;

import com.example.springboot.entity.SystemConfig;
import com.example.springboot.mapper.SystemConfigMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public int insert(SystemConfig config) {
        return systemConfigMapper.insert(config);
    }

    public int updateValueByKey(String key, String value) {
        int updated = systemConfigMapper.updateValueByKey(key, value);
        if (updated == 0) {
            SystemConfig sc = new SystemConfig();
            sc.setKey(key);
            sc.setValue(value);
            sc.setDescription("auto-created by updateValueByKey upsert");
            return systemConfigMapper.insert(sc);
        }
        return updated;
    }

    public int deleteByKey(String key) {
        return systemConfigMapper.deleteByKey(key);
    }

    /** 读取配置为 BigDecimal，容错返回默认值 */
    public BigDecimal getDecimalOrDefault(String key, BigDecimal defaultValue) {
        SystemConfig cfg = systemConfigMapper.selectByKey(key);
        if (cfg == null || cfg.getValue() == null) {
            return defaultValue;
        }
        try {
            return new BigDecimal(cfg.getValue().trim());
        } catch (Exception ignored) {
            return defaultValue;
        }
    }
}


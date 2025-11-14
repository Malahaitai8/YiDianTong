package com.example.springboot.mapper;

import com.example.springboot.entity.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SystemConfigMapper {
    List<SystemConfig> selectAll();

    SystemConfig selectById(Long id);
    
    SystemConfig selectByKey(String key);

    int insert(SystemConfig config);

    int updateValueByKey(String key, String value);
    
    int deleteByKey(String key);
}


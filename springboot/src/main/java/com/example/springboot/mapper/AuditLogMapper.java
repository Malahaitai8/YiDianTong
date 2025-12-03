package com.example.springboot.mapper;

import com.example.springboot.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper {
    int insert(AuditLog auditLog);
}

































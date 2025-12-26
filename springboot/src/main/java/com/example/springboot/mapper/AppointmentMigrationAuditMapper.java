package com.example.springboot.mapper;

import com.example.springboot.entity.AppointmentMigrationAudit;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AppointmentMigrationAuditMapper {
    int insert(AppointmentMigrationAudit audit);
}



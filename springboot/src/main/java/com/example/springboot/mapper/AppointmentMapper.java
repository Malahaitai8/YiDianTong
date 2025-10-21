package com.example.springboot.mapper;

import com.example.springboot.entity.Appointment;

import java.util.List;

public interface AppointmentMapper {
    List<Appointment> selectAll();

    Appointment selectById(Long id);

    /** 新增预约 */
    int insert(Appointment appointment);

    /** 根据主键删除预约 */
    int deleteById(Long id);

    /** 查询当前患者的预约列表 */
    List<Appointment> selectByPatientId(Long patientId);

    /** 更新预约状态 */
    int updateStatus(Long id, String status);
}


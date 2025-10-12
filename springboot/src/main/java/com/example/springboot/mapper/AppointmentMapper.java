package com.example.springboot.mapper;

import com.example.springboot.entity.Appointment;

import java.util.List;

public interface AppointmentMapper {
    List<Appointment> selectAll();

    Appointment selectById(Long id);
}


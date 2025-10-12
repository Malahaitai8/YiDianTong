package com.example.springboot.service;

import com.example.springboot.entity.Appointment;
import com.example.springboot.mapper.AppointmentMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    @Resource
    private AppointmentMapper appointmentMapper;

    public List<Appointment> selectAll() {

        List<Appointment> list = appointmentMapper.selectAll();

        return list;
    }

    public Appointment selectById(Long id) {
        Appointment appointment = appointmentMapper.selectById(id);
        return appointment;
    }
}


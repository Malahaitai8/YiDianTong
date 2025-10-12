package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Appointment;
import com.example.springboot.service.AppointmentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Appointment> list = appointmentService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Appointment appointment = appointmentService.selectById(id);
        return Result.success(appointment);
    }

}


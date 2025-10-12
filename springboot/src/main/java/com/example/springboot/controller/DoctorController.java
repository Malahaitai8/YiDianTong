package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Doctor;
import com.example.springboot.service.DoctorService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    @Resource
    private DoctorService doctorService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Doctor> list = doctorService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Doctor doctor = doctorService.selectById(id);
        return Result.success(doctor);
    }

}


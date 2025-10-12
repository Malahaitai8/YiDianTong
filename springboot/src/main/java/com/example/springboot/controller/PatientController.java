package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    @Resource
    private PatientService patientService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Patient> list = patientService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Patient patient = patientService.selectById(id);
        return Result.success(patient);
    }

}


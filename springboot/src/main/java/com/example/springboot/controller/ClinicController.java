package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Clinic;
import com.example.springboot.service.ClinicService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/clinic")
public class ClinicController {

    @Resource
    private ClinicService clinicService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Clinic> list = clinicService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Clinic clinic = clinicService.selectById(id);
        return Result.success(clinic);
    }

}


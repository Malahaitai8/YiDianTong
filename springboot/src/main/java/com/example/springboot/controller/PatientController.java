package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Patient;
import com.example.springboot.service.PatientService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/login")
    public Result login(@RequestParam String phoneNumber, @RequestParam String password) {
        Patient patient = patientService.login(phoneNumber, password);
        return Result.success(patient);
    }

    @PostMapping("/register")
    public Result register(@RequestParam String name,
                          @RequestParam String phoneNumber,
                          @RequestParam String password,
                          @RequestParam(required = false) String idCardNumber,
                          @RequestParam String role) {
        if (!role.equals("STUDENT") && !role.equals("TEACHER") && !role.equals("OTHER")) {
            return Result.error("角色只能是 STUDENT 或 TEACHER 或 OTHER");
        }
        patientService.register(name, phoneNumber, password, idCardNumber, role);
        return Result.success();
    }
}



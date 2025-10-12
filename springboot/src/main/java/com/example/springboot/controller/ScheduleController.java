package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Schedule;
import com.example.springboot.service.ScheduleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;


    @GetMapping("/selectAll")
    public Result selectAll() {

        List<Schedule> list = scheduleService.selectAll();

        return Result.success(list);
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Long id) {

        Schedule schedule = scheduleService.selectById(id);
        return Result.success(schedule);
    }

}


package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ScheduleDTO;
import com.example.springboot.service.ScheduleService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @GetMapping("/week")
    public Result getWeekSchedule() {
        List<ScheduleDTO> schedules = scheduleService.getWeekSchedule();
        return Result.success(schedules);
    }
}
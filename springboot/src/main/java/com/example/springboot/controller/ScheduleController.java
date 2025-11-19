package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.dto.ScheduleDTO;
import com.example.springboot.entity.Schedule;
import com.example.springboot.service.ScheduleService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "排班管理", description = "医生排班查询相关接口")
@RestController
@RequestMapping("/schedule")
@SecurityRequirement(name = "bearer-jwt")
public class ScheduleController {

    @Resource
    private ScheduleService scheduleService;

    @Operation(summary = "获取本周排班", description = "返回一周内医生的排班计划")
    @GetMapping("/week")
    public Result getWeekSchedule() {
        List<ScheduleDTO> schedules = scheduleService.getWeekSchedule();
        return Result.success(schedules);
    }

    @Operation(summary = "根据ID查询排班详情", description = "返回指定ID的排班详细信息")
    @GetMapping("/selectById/{id}")
    public Result getScheduleById(@PathVariable Long id) {
        Schedule schedule = scheduleService.getScheduleById(id);
        return Result.success(schedule);
    }
}
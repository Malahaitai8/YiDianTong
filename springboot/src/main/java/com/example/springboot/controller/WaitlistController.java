package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.entity.Waitlist;
import com.example.springboot.service.WaitlistService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@RestController
@RequestMapping("/waitlist")
public class WaitlistController {

    @Resource
    private WaitlistService waitlistService;


    /** 加入候补队列 */
    @PostMapping
    public Result addToQueue(@RequestBody Waitlist request) {
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        Waitlist w = waitlistService.addToQueue(patientId, request.getScheduleId());
        return Result.success(w);
    }

    /** 查看我的候补列表 */
    @GetMapping("/me")
    public Result myQueue() {
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        return Result.success(waitlistService.listByPatient(patientId));
    }

    /** 弹出队首（管理员或系统任务） */
    @PostMapping("/next/{scheduleId}")
    public Result popNext(@PathVariable Long scheduleId) {
        Waitlist next = waitlistService.popNext(scheduleId);
        return Result.success(next);
    }
}


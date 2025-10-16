package com.example.springboot.controller;


import com.example.springboot.common.Result;
import com.example.springboot.dto.CreateWaitlistRequest; // <-- [新增] 导入
import com.example.springboot.entity.Waitlist;
import com.example.springboot.service.WaitlistService;
import jakarta.annotation.Resource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize; // <-- [新增] 导入
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "候补队列", description = "候补队列相关接口")
@RestController
@RequestMapping("/waitlist")
@SecurityRequirement(name = "bearer-jwt")
public class WaitlistController {

    @Resource
    private WaitlistService waitlistService;


    /** 加入候补队列 */
    @Operation(summary = "加入候补队列", description = "当号源已满时，患者可加入候补队列")
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')") // <-- [新增] 权限
    public Result addToQueue(@jakarta.validation.Valid @RequestBody CreateWaitlistRequest request) { // <-- [修改] 签名
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();

        // TODO: 校验该排班是否真的已满 (available_slots <= 0)

        Waitlist w = waitlistService.addToQueue(patientId, request.getScheduleId());
        return Result.success(w);
    }

    /** 查看我的候补列表 */
    @Operation(summary = "查看我的候补", description = "查看当前登录患者的候补记录")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')") // <-- [新增] 权限
    public Result myQueue() {
        Long patientId = com.example.springboot.config.SecurityUtils.getCurrentUserId();
        return Result.success(waitlistService.listByPatient(patientId));
    }

    /** 弹出队首（管理员或系统任务） */
    @Operation(summary = "弹出队首并创建预约", description = "管理员在有空位时调用，自动创建预约")
    @PostMapping("/next/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')") // <-- [新增] 权限
    public Result popNext(@PathVariable Long scheduleId) {
        Waitlist next = waitlistService.popNext(scheduleId);
        return Result.success(next);
    }
}